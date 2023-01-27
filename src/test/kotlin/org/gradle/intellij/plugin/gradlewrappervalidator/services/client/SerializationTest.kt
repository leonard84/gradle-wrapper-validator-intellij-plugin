package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SerializationTest {
    companion object {
        val singleJson = """
             {
              "version" : "8.1-20230119104422+0000",
              "buildTime" : "20230119104422+0000",
              "current" : false,
              "snapshot" : true,
              "nightly" : true,
              "releaseNightly" : false,
              "activeRc" : false,
              "rcFor" : "",
              "milestoneFor" : "",
              "broken" : false,
              "downloadUrl" : "https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-bin.zip",
              "checksumUrl" : "https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-bin.zip.sha256",
              "wrapperChecksumUrl" : "https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-wrapper.jar.sha256"
            }
        """.trimIndent()
    }

    @Test
    fun testSingleDeserialization() {
        val serviceEntry = Json.decodeFromString<ServiceEntry>(singleJson)

        validateSingleEntry(serviceEntry)
    }

    private fun validateSingleEntry(serviceEntry: ServiceEntry) {
        assertThat(serviceEntry.version).isEqualTo("8.1-20230119104422+0000")
        assertThat(serviceEntry.buildTime).isEqualTo("20230119104422+0000")
        assertThat(serviceEntry.current).isEqualTo(false)
        assertThat(serviceEntry.snapshot).isEqualTo(true)
        assertThat(serviceEntry.nightly).isEqualTo(true)
        assertThat(serviceEntry.releaseNightly).isEqualTo(false)
        assertThat(serviceEntry.activeRc).isEqualTo(false)
        assertThat(serviceEntry.rcFor).isEqualTo("")
        assertThat(serviceEntry.milestoneFor).isEqualTo("")
        assertThat(serviceEntry.broken).isEqualTo(false)
        assertThat(serviceEntry.downloadUrl).isEqualTo("https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-bin.zip")
        assertThat(serviceEntry.checksumUrl).isEqualTo("https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-bin.zip.sha256")
        assertThat(serviceEntry.wrapperChecksumUrl).isEqualTo("https://services.gradle.org/distributions-snapshots/gradle-8.1-20230119104422+0000-wrapper.jar.sha256")
    }

    @Test
    fun testArrayDeserialization() {
        val json = """
            [
            $singleJson,
            {
              "version" : "0.7",
              "buildTime" : "20090720085013+0200",
              "current" : false,
              "snapshot" : false,
              "nightly" : false,
              "releaseNightly" : false,
              "activeRc" : false,
              "rcFor" : "",
              "milestoneFor" : "",
              "broken" : false,
              "downloadUrl" : "https://services.gradle.org/distributions/gradle-0.7-bin.zip",
              "checksumUrl" : "https://services.gradle.org/distributions/gradle-0.7-bin.zip.sha256"
            }
            ]
        """.trimIndent()


        val serviceEntries = Json.decodeFromString<List<ServiceEntry>>(json)

        assertThat(serviceEntries.size).isEqualTo(2)
        validateSingleEntry(serviceEntries[0])
        val secondEntry = serviceEntries[1]
        assertThat(secondEntry.version).isEqualTo("0.7")
        assertThat(secondEntry.buildTime).isEqualTo("20090720085013+0200")
        assertThat(secondEntry.current).isEqualTo(false)
        assertThat(secondEntry.snapshot).isEqualTo(false)
        assertThat(secondEntry.nightly).isEqualTo(false)
        assertThat(secondEntry.releaseNightly).isEqualTo(false)
        assertThat(secondEntry.activeRc).isEqualTo(false)
        assertThat(secondEntry.rcFor).isEqualTo("")
        assertThat(secondEntry.milestoneFor).isEqualTo("")
        assertThat(secondEntry.broken).isEqualTo(false)
        assertThat(secondEntry.downloadUrl).isEqualTo("https://services.gradle.org/distributions/gradle-0.7-bin.zip")
        assertThat(secondEntry.checksumUrl).isEqualTo("https://services.gradle.org/distributions/gradle-0.7-bin.zip.sha256")
        assertThat(secondEntry.wrapperChecksumUrl).isNull()
    }
}
