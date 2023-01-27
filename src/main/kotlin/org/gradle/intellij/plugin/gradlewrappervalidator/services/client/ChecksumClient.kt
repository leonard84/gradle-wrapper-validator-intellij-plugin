package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.stream.Collectors.toList

class ChecksumClient(private val serviceUrl: URI = URI(GRADLE_SERVICES_URL)) {

    companion object {
        private const val GRADLE_SERVICES_URL = "https://services.gradle.org/versions/all"
    }

    private val client = HttpClient.newBuilder()
        // We have to follow redirects because the service redirects the download urls to a CDN
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    fun getWrapperChecksums(
        etag: String? = null,
        knownHashes: Map<String, String> = emptyMap()
    ): WrapperChecksumsResult {
        val lookupResult = getServiceEntries(etag)
        val newEntries = lookupResult.serviceEntries.stream()
            .filter { it.wrapperChecksumUrl != null }
            .filter { !knownHashes.containsKey(it.version) }
            .collect(toList())

        val newHashes = runBlocking {
            newEntries.asFlow().take(1).map { entry ->
                entry to loadContentAsync(entry.wrapperChecksumUrl!!)
            }.toList()
        }

        val resultHashes = knownHashes.toMutableMap()
        newHashes.forEach { resultHashes[it.first.version] = it.second }
        return WrapperChecksumsResult(resultHashes.toMap(), lookupResult.etag, lookupResult.lastUpdate)
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

    private suspend fun loadContentAsync(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        return response.thenApply { it.body() }.await()
    }
}

val <T> HttpResponse<T>.etag: String?
    get() = headers().firstValue("ETag").orElse(null)
