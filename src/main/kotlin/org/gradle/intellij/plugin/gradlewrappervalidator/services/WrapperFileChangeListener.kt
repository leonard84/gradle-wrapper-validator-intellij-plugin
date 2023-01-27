package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class WrapperFileChangeListener : AsyncFileListener {
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        events.stream()
            .filter { it.path.endsWith("gradle/wrapper/gradle-wrapper.jar") }
            .forEach { println("wrapper.jar changed") }

        return null
    }

}
