package org.gradle.intellij.plugin.gradlewrappervalidator.services

import org.gradle.intellij.plugin.gradlewrappervalidator.WrapperValidatorBundle

class WrapperValidatorApplicationService {

    init {
        println(WrapperValidatorBundle.message("applicationService"))

        System.getenv("CI")
            ?: TODO("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }
}
