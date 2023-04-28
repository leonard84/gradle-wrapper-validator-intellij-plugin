package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
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

    fun validateWrapper(wrapperVFile: VirtualFile) {
        ReadAction.nonBlocking(Callable {
            val fileIndex = ProjectFileIndex.getInstance(project)
            if (!fileIndex.isInContent(wrapperVFile)) return@Callable ValidationResult.NotApplicable.withVirtualFile(
                wrapperVFile
            )
            ProgressManager.checkCanceled()
            val hash = Hasher.sha256(wrapperVFile.inputStream)
            return@Callable WrapperValidatorApplicationService.instance.validateWrapper(hash)
                .withVirtualFile(wrapperVFile)
        })
            .withDocumentsCommitted(project)
            .finishOnUiThread(ModalityState.NON_MODAL) { result ->
                when (result.validationResult) {
                    is ValidationResult.Valid -> {
                        Notification(
                            "WrapperValidationNotificationGroup",
                            WrapperValidatorBundle.message("wrapperValidationNotificationSuccessTitle"),
                            WrapperValidatorBundle.message(
                                "wrapperValidationNotificationSuccessMessage",
                                result.validationResult.version
                            ),
                            NotificationType.INFORMATION
                        ).notify(project)
                    }

                    is ValidationResult.Invalid -> {
                        if (WrapperValidatorApplicationService.instance.state.renameWrapperOnValidationFailure) {
                            renameWrapperFile(result)

                            if (WrapperValidatorApplicationService.instance.state.showAlertBoxOnValidationFailure) {
                                Messages.showErrorDialog(
                                    project,
                                    WrapperValidatorBundle.message(
                                        "wrapperValidationAlertMessage",
                                        result.validationResult.invalidHash
                                    ),
                                    WrapperValidatorBundle.message("wrapperValidationAlertFailedTitle")
                                )
                            }
                        } else if (WrapperValidatorApplicationService.instance.state.showAlertBoxOnValidationFailure) {
                            Messages.showOkCancelDialog(
                                project,
                                WrapperValidatorBundle.message(
                                    "wrapperValidationAlertMessageWithRename",
                                    result.validationResult.invalidHash
                                ),
                                WrapperValidatorBundle.message("wrapperValidationAlertFailedTitle"),
                                WrapperValidatorBundle.message("wrapperValidationAlertRenameButton"),
                                WrapperValidatorBundle.message("wrapperValidationAlertCancelButton"),
                                Messages.getErrorIcon()
                            ).let {
                                when (it) {
                                    Messages.OK -> renameWrapperFile(result)
                                    else -> Unit
                                }
                            }
                        }
                        Notification(
                            "WrapperValidationNotificationGroup",
                            WrapperValidatorBundle.message("wrapperValidationNotificationFailedTitle"),
                            WrapperValidatorBundle.message(
                                "wrapperValidationNotificationFailedMessage",
                                result.validationResult.invalidHash
                            ),
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

    private fun renameWrapperFile(result: VirtualFileWithValidationResult) {
        ApplicationManager.getApplication().runWriteAction {
            result.virtualFile.let {
                it.rename(this, it.nameWithoutExtension + "-jar.invalid")
            }
        }
    }

    override fun dispose() {
        // No idea what to do here
    }
}
