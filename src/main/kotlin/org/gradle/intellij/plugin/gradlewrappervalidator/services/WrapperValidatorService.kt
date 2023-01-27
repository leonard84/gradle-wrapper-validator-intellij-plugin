package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.project.Project
import org.gradle.intellij.plugin.gradlewrappervalidator.WrapperValidatorBundle

class WrapperValidatorService(project: Project) {

    init {
        println(WrapperValidatorBundle.message("projectService", project.name))

        System.getenv("CI")
            ?: TODO("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    /**
     * Chosen by fair dice roll, guaranteed to be random.
     */
    fun getRandomNumber() = 4
}
