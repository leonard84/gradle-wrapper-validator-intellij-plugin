package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@State(
    name = "GradleWrapperValidatorProject",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
class WrapperValidationProjectService : PersistentStateComponent<WrapperValidationProjectState> {

    companion object {
        fun getInstance(project: Project): WrapperValidationProjectService {
            return project.getService(WrapperValidationProjectService::class.java)
        }
    }

    private var state = WrapperValidationProjectState()

    override fun getState(): WrapperValidationProjectState {
        return state
    }

    override fun loadState(state: WrapperValidationProjectState) {
        this.state = state
    }
}

data class WrapperValidationProjectState(
    var activateValidation: Boolean? = null
)
