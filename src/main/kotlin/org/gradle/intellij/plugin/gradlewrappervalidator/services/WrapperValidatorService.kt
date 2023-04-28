package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import org.gradle.intellij.plugin.gradlewrappervalidator.WrapperValidatorBundle
import java.util.concurrent.Callable

class WrapperValidatorService(private val project: Project) : Disposable {
    companion object {
        private val LOG = logger<WrapperValidatorService>()
    }

    init {
        println(WrapperValidatorBundle.message("projectService", project.name))
    }

    fun validateWrapper(wrapperVFile: VirtualFile) {
        val result = ReadAction.nonBlocking(Callable {
            val fileIndex = ProjectFileIndex.getInstance(project)
            if (!fileIndex.isInContent(wrapperVFile)) return@Callable ValidationResult.NotApplicable
            ProgressManager.checkCanceled()
            val hash = Hasher.sha256(wrapperVFile.inputStream)
            return@Callable WrapperValidatorApplicationService.instance.validateWrapper(hash)
        })
        result.withDocumentsCommitted(project).finishOnUiThread(ModalityState.NON_MODAL) {
            when (it) {
                is ValidationResult.Valid -> {
                    Notification(
                        "WrapperValidationNotificationGroup",
                        WrapperValidatorBundle.message("wrapperValidationNotificationSuccessTitle"),
                        WrapperValidatorBundle.message("wrapperValidationNotificationSuccessMessage", it.version),
                        NotificationType.INFORMATION
                    ).notify(project)
                }

                is ValidationResult.Invalid -> {
                    Messages.showErrorDialog(
                        project,
                        WrapperValidatorBundle.message("wrapperValidationAlertMessage", it.invalidHash),
                        WrapperValidatorBundle.message("wrapperValidationAlertFailedTitle")
                    )
                    Notification(
                        "WrapperValidationNotificationGroup",
                        WrapperValidatorBundle.message("wrapperValidationNotificationFailedTitle"),
                        WrapperValidatorBundle.message("wrapperValidationNotificationFailedMessage", it.invalidHash),
                        NotificationType.ERROR
                    ).notify(project)
                    LOG.warn("Wrapper validation failed for ${wrapperVFile.path}")
                }

                ValidationResult.NotApplicable -> {
                    // Do nothing
                }
            }
        }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    override fun dispose() {
        // No idea what to do here
    }
}
