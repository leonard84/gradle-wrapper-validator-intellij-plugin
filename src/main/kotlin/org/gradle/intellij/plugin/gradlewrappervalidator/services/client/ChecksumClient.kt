package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Sha256
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Version
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.stream.Collectors.toList

class ChecksumClient(private val serviceUrl: URI = URI(GRADLE_SERVICES_URL)) : AutoCloseable {

    companion object {
        private const val GRADLE_SERVICES_URL = "https://services.gradle.org/versions/all"
    }

    private val executorService = Executors.newFixedThreadPool(4)

    private val client = HttpClient.newBuilder()
        // We have to follow redirects because the service redirects the download urls to a CDN
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()


    fun getWrapperChecksums(
        etag: String? = null,
        knownHashes: Map<Version, Sha256> = emptyMap()
    ): WrapperChecksumsResult {
        val lookupResult = getServiceEntries(etag)
        val newEntries = lookupResult.serviceEntries.stream()
            .filter { it.wrapperChecksumUrl != null }
            .filter { !knownHashes.containsKey(Version(it.version)) }
            .collect(toList())

        val newHashes = newEntries
            .map { entry ->
                loadContentAsync(entry.wrapperChecksumUrl!!).thenApply { entry to it }
            }

        CompletableFuture.allOf(*newHashes.toTypedArray()).join()

        val resultHashes =
            newHashes.map { it.get() }.associateTo(mutableMapOf()) { Version(it.first.version) to Sha256(it.second) }
        return WrapperChecksumsResult(
            resultHashes.toMap(),
            lookupResult.etag,
            lookupResult.lastUpdate
        )
    }

    fun getServiceEntries(etag: String? = null): ServiceEntriesResult {
        val request = HttpRequest.newBuilder()
            .uri(serviceUrl)
            .also { builder ->
                etag?.let { builder.header("If-None-Match", it) }
            }
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() == 304) {
            ServiceEntriesResult(listOf(), response.etag, Instant.now())
        } else {
            val serviceEntries = Json.decodeFromString<List<ServiceEntry>>(response.body())
            ServiceEntriesResult(serviceEntries, response.etag, Instant.now())
        }
    }

    private fun loadContentAsync(url: String): CompletableFuture<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        return response.thenApply { it.body() }
    }

    override fun close() {
        executorService.shutdownNow()
    }
}

val <T> HttpResponse<T>.etag: String?
    get() = headers().firstValue("ETag").orElse(null)
