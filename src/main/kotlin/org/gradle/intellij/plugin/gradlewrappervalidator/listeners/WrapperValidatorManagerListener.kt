package org.gradle.intellij.plugin.gradlewrappervalidator.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.gradle.intellij.plugin.gradlewrappervalidator.services.WrapperValidatorService

internal class WrapperValidatorManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<WrapperValidatorService>()

        System.getenv("CI")
            ?: TODO("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }
}
