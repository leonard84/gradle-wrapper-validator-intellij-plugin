package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ServiceEntry(
    val version: String,
    val buildTime: String,
    val current: Boolean,
    val snapshot: Boolean,
    val nightly: Boolean,
    val releaseNightly: Boolean,
    val activeRc: Boolean,
    val rcFor: String,
    val milestoneFor: String,
    val broken: Boolean,
    val downloadUrl: String,
    val checksumUrl: String,
    val wrapperChecksumUrl: String? = null
)

interface LookupResult {
    val etag: String?
    val lastUpdate: Instant
}

data class ServiceEntriesResult(
    val serviceEntries: List<ServiceEntry>,
    override val etag: String?,
    override val lastUpdate: Instant
) : LookupResult

data class WrapperChecksumsResult(
    val wrapperChecksums: Map<String, String>,
    override val etag: String?,
    override val lastUpdate: Instant
) : LookupResult
