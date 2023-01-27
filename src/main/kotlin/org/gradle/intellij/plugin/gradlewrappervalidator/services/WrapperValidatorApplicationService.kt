package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger
import org.gradle.intellij.plugin.gradlewrappervalidator.services.client.ChecksumClient

@Storage("WrapperValidator.xml")
class WrapperValidatorApplicationService : PersistentStateComponent<WrapperValidatorApplicationState> {
    private val LOG = logger<WrapperValidatorApplicationService>()

    private var state = WrapperValidatorApplicationState()
    private var checksumClient = ChecksumClient()
    override fun getState(): WrapperValidatorApplicationState {
        return this.state
    }

    override fun loadState(state: WrapperValidatorApplicationState) {
        this.state = state
    }

    fun updateNow() {
        LOG.debug("Updating wrapper hashes. (etag: ${state.etagOfLastUpdate}, number of wrapper hashes: ${state.wrapperHashes.size})")
        val response = checksumClient.getWrapperChecksums(state.etagOfLastUpdate, state.wrapperHashes)
        state.lastUpdate = response.lastUpdate
        state.etagOfLastUpdate = response.etag
        state.wrapperHashes.putAll(response.wrapperChecksums)
        LOG.debug("Updated wrapper hashes. (etag: ${state.etagOfLastUpdate}, number of wrapper hashes: ${state.wrapperHashes.size})")
    }

    fun validateWrapper(wrapperHash: String): Boolean {
        val valid = state.wrapperHashes.containsValue(wrapperHash)
        return if (!valid) {
            updateNow()
            state.wrapperHashes.containsValue(wrapperHash)
        } else
            true
    }
}
