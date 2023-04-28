package org.gradle.intellij.plugin.gradlewrappervalidator.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorApplicationService
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorService
import java.nio.file.Path

private const val GRADLE_WRAPPER_GRADLE_WRAPPER_JAR = "gradle/wrapper/gradle-wrapper.jar"

internal class WrapperValidatorManagerListener : ProjectManagerListener {

    companion object {
        private val LOG = logger<WrapperValidatorManagerListener>()
    }

    override fun projectOpened(project: Project) {
        val wrapperValidatorService = project.service<WrapperValidatorService>()

        val connection = project.messageBus.connect(wrapperValidatorService)
        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            WrapperListener(wrapperValidatorService)
        )

        //WrapperValidationProjectService.getInstance(project).state.activateValidation

        project.basePath?.let { projectBasePath ->
            LocalFileSystem.getInstance()
                .refreshAndFindFileByNioFile(Path.of(projectBasePath, GRADLE_WRAPPER_GRADLE_WRAPPER_JAR))
                ?.let {
                    LOG.debug("Found gradle-wrapper.jar in project base path.")
                    wrapperValidatorService.validateWrapper(it)
                }
        }

        WrapperValidatorApplicationService.instance.update()
    }
}

class WrapperListener(private val wrapperValidatorService: WrapperValidatorService) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        events.stream()
            .filter { it.path.endsWith(GRADLE_WRAPPER_GRADLE_WRAPPER_JAR) }
            .map { vFileEvent ->
                when (vFileEvent) {
                    is VFileCreateEvent -> vFileEvent.file
                    is VFileContentChangeEvent -> vFileEvent.file
                    else -> null
                }
            }
            .filter { it != null }
            .forEach {
                ProgressManager.checkCanceled()
                wrapperValidatorService.validateWrapper(it!!)
            }

    }
}
