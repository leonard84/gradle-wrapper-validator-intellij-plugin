package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import kotlinx.serialization.Serializable

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
