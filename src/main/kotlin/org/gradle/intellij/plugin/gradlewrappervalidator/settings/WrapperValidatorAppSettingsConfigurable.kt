package org.gradle.intellij.plugin.gradlewrappervalidator.settings

import com.intellij.openapi.options.Configurable
import org.gradle.intellij.plugin.gradlewrappervalidator.WrapperValidatorBundle
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorApplicationService
import javax.swing.JComponent

class WrapperValidatorAppSettingsConfigurable : Configurable {

    private var component: WrapperValidatorAppSettingsComponent? = null
    override fun createComponent(): JComponent? {
        component = WrapperValidatorAppSettingsComponent()
        return component?.panel
    }

    override fun isModified(): Boolean {
        val state = WrapperValidatorApplicationService.instance.state
        return component?.let {
            state.activateValidationByDefault != it.activateByDefault.isSelected
                || state.showAlertBoxOnValidationFailure != it.showAlertBoxOnValidationFailure.isSelected
                || state.renameWrapperOnValidationFailure != it.renameWrapperOnValidationFailure.isSelected
        } ?: false
    }

    override fun apply() {
        val state = WrapperValidatorApplicationService.instance.state
        component?.let {
            state.activateValidationByDefault = it.activateByDefault.isSelected
            state.showAlertBoxOnValidationFailure = it.showAlertBoxOnValidationFailure.isSelected
            state.renameWrapperOnValidationFailure = it.renameWrapperOnValidationFailure.isSelected
        }
    }

    override fun reset() {
        val state = WrapperValidatorApplicationService.instance.state
        component?.let {
            it.activateByDefault.isSelected = state.activateValidationByDefault
            it.showAlertBoxOnValidationFailure.isSelected = state.showAlertBoxOnValidationFailure
            it.renameWrapperOnValidationFailure.isSelected = state.renameWrapperOnValidationFailure
        }
    }

    override fun getDisplayName(): String = WrapperValidatorBundle.message("settingsDisplayName")

    override fun disposeUIResources() {
        component = null
    }
}
