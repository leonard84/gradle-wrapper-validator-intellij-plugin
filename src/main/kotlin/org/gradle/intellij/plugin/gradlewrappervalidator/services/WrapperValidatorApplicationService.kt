package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger
import org.gradle.intellij.plugin.gradlewrappervalidator.services.client.ChecksumClient


@State(
    name = "GradleWrapperValidator",
    storages = [Storage("GradleWrapperValidator.xml")]
)
class WrapperValidatorApplicationService : PersistentStateComponent<WrapperValidatorApplicationState>, Disposable {

    companion object {
        private val LOG = logger<WrapperValidatorApplicationService>()

        val instance: WrapperValidatorApplicationService
            get() = ApplicationManager.getApplication().getService(WrapperValidatorApplicationService::class.java)
    }

    private var state = WrapperValidatorApplicationState()
    private var checksumClient = ChecksumClient()
    override fun getState(): WrapperValidatorApplicationState {
        return this.state
    }

    override fun loadState(state: WrapperValidatorApplicationState) {
        this.state = state
    }

    fun updateNow(force: Boolean = false) {
        LOG.debug("Updating wrapper hashes (force: ${force}). (etag: ${state.etagOfLastUpdate}, number of wrapper hashes: ${state.wrapperHashes.size})")
        val response = checksumClient.getWrapperChecksums(
            if (force) null else state.etagOfLastUpdate,
            state.wrapperHashes
        )
        state.lastUpdate = response.lastUpdate
        state.etagOfLastUpdate = response.etag
        state.wrapperHashes.putAll(response.wrapperChecksums)
        LOG.debug("Updated wrapper hashes. (etag: ${state.etagOfLastUpdate}, number of wrapper hashes: ${state.wrapperHashes.size})")
    }

    fun update() {
        ApplicationManager.getApplication().executeOnPooledThread {
            updateNow()
        }
    }

    fun validateWrapper(wrapperHash: String): ValidationResult {
        val valid = state.wrapperHashes[wrapperHash]
        return if (valid == null) {
            updateNow()
            state.wrapperHashes[wrapperHash]?.let { ValidationResult.Valid(it) }
                ?: ValidationResult.invalid(wrapperHash)
        } else
            ValidationResult.valid(valid)
    }

    override fun dispose() {
        checksumClient.close()
    }
}
