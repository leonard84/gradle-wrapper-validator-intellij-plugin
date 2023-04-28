package org.gradle.intellij.plugin.gradlewrappervalidator.settings

import com.intellij.util.ui.FormBuilder
import org.gradle.intellij.plugin.gradlewrappervalidator.WrapperValidatorBundle
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorApplicationService
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

class WrapperValidatorAppSettingsComponent() {
    val panel: JPanel
    val activateByDefault = JCheckBox(WrapperValidatorBundle.message("settingsActivateValidationByDefault"))
    val activateForCurrentProject =
        JCheckBox(WrapperValidatorBundle.message("settingsActivateValidationForCurrentProject"))
    val showAlertBoxOnValidationFailure =
        JCheckBox(WrapperValidatorBundle.message("settingsShowAlertBoxOnValidationFailure"))
    var renameWrapperOnValidationFailure =
        JCheckBox(WrapperValidatorBundle.message("settingsRenameWrapperOnValidationFailure"))
    private val checksumInfo = JLabel()
    private val lastUpdateInfo = JLabel()
    private val etagInfo = JLabel()
    private val refreshNow = JButton(WrapperValidatorBundle.message("settingsRefreshNow"))

    init {
        panel = FormBuilder.createFormBuilder()
            .addComponent(activateByDefault)
            .addComponent(activateForCurrentProject)
            .addComponent(showAlertBoxOnValidationFailure)
            .addComponent(renameWrapperOnValidationFailure)
            .addComponent(checksumInfo)
            .addComponent(lastUpdateInfo)
            .addComponent(etagInfo)
            .addComponent(refreshNow)
            .panel

        updateInfo()
        refreshNow.addActionListener {
            WrapperValidatorApplicationService.instance.updateNow(true)
            updateInfo()
        }
    }

    private fun updateInfo() {
        val state = WrapperValidatorApplicationService.instance.state
        checksumInfo.text = WrapperValidatorBundle.message(
            "settingsChecksumInfo",
            state.wrapperHashes.size
        )
        lastUpdateInfo.text = WrapperValidatorBundle.message(
            "settingsLastUpdateInfo",
            state.lastUpdate ?: "n/a"
        )
        etagInfo.text = WrapperValidatorBundle.message(
            "settingsLastUpdateEtagInfo",
            state.etagOfLastUpdate ?: "n/a"
        )
    }
}
