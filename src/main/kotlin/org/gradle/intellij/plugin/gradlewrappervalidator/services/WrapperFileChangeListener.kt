package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class WrapperFileChangeListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val findAny = events.stream()
            .filter { it.path.endsWith("gradle/wrapper/gradle-wrapper.jar") }
            .peek { println("wrapper.jar changed (refresh: ${it.isFromRefresh}, save: ${it.isFromSave}, valid: ${it.isValid}, type: ${it.javaClass})") }
            .findAny()
        ProgressManager.checkCanceled()
        return findAny.map { WrapperChangeValidationApplier(it) }.orElse(null)
    }

}

class WrapperChangeValidationApplier(private val vFileEvent: VFileEvent) : AsyncFileListener.ChangeApplier {
    override fun afterVfsChange() {
        val virtualFile = when (vFileEvent) {
            is VFileCreateEvent -> vFileEvent.file
            is VFileContentChangeEvent -> vFileEvent.file
            else -> null
        }

    }
}
