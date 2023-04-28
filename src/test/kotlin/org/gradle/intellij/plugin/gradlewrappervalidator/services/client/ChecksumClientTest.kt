package org.gradle.intellij.plugin.gradlewrappervalidator.services.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.assertj.core.api.Assertions.assertThat
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Sha256
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Version
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URI

class ChecksumClientTest {
    @JvmField
    @Rule
    val wireMockRule = WireMockRule()

    lateinit var serviceUrl: String

    @Before
    fun setup() {
        val allVersions = "/versions/all"
        serviceUrl = wireMockRule.baseUrl() + allVersions
        wireMockRule.stubFor(
            get(allVersions).willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withHeader("ETag", "1234")
                    .withBody(
                        httpResponseFixture("serviceAll.json").replace(
                            "https://services.gradle.org",
                            wireMockRule.baseUrl()
                        )
                    )
            )
        )
        wireMockRule.stubFor(
            get("/distributions/gradle-7.6-wrapper.jar.sha256").willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", wireMockRule.baseUrl() + "/cdn/gradle-7.6-wrapper.jar.sha256")
            )
        )
        wireMockRule.stubFor(
            get("/cdn/gradle-7.6-wrapper.jar.sha256").willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/octet-stream")
                    .withBody(httpResponseFixture("gradle-7.6-wrapper.jar.sha256"))
            )
        )
    }

    private fun httpResponseFixture(fixtureName: String) =
        ChecksumClient::class.java.getResource("/http/responses/$fixtureName")!!.readText()


    @Test
    fun testGetServiceEntries() {
        val checksumClient = ChecksumClient(URI(serviceUrl))
        val response = checksumClient.getServiceEntries()
        assertThat(response).isNotNull
        assertThat(response.etag).isEqualTo("1234")
        assertThat(response.serviceEntries)
            .hasSize(2)
            .extracting("version")
            .containsExactly("7.6", "0.7")
    }

    @Test
    fun testGetWrapperChecksums() {
        val checksumClient = ChecksumClient(URI(serviceUrl))
        val response = checksumClient.getWrapperChecksums()
        assertThat(response).isNotNull
        assertThat(response.etag).isEqualTo("1234")
        assertThat(response.wrapperChecksums)
            .hasSize(1)
            .containsExactlyEntriesOf(mapOf(Version("7.6") to Sha256("c5a643cf80162e665cc228f7b16f343fef868e47d3a4836f62e18b7e17ac018a")))
    }
}
