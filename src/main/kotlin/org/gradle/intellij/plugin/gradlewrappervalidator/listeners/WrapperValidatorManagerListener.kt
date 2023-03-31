package org.gradle.intellij.plugin.gradlewrappervalidator.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorApplicationService
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorService

internal class WrapperValidatorManagerListener : ProjectManagerListener {

    companion object {
        private val LOG = logger<WrapperValidatorManagerListener>()
    }

    override fun projectOpened(project: Project) {
        LOG.info("Project opened: ${project.name}")
        val wrapperValidatorService = project.service<WrapperValidatorService>()

        val connection = project.messageBus.connect(wrapperValidatorService)
        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            WrapperListener(wrapperValidatorService)
        )
        GlobalSearchScope.projectScope(project).let { scope ->
            FilenameIndex.getFilesByName(project, "gradle-wrapper.jar", scope).forEach {
                LOG.info("Wrapper found: ${it.virtualFile.path}")
                wrapperValidatorService.validateWrapper(it.virtualFile)
            }
        }
        WrapperValidatorApplicationService.instance.update()
    }
}

class WrapperListener(private val wrapperValidatorService: WrapperValidatorService) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        events.stream()
            .filter { it.path.endsWith("gradle/wrapper/gradle-wrapper.jar") }
            .peek {
                println("VFS event: ${it.path} (refresh: ${it.isFromRefresh}, save: ${it.isFromSave}, valid: ${it.isValid}, type: ${it.javaClass})")
            }
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
