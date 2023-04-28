package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.vfs.VirtualFile

sealed class ValidationResult {
    /* Indicates that a validation could not be performed, e.g., file was already deleted. */
    object NotApplicable : ValidationResult()
    data class Invalid(val invalidHash: String) : ValidationResult()
    data class Valid(val version: String) : ValidationResult()

    companion object {
        fun valid(version: String): ValidationResult = Valid(version)

        fun invalid(invalidHash: String): ValidationResult = Invalid(invalidHash)
    }

    fun withVirtualFile(virtualFile: VirtualFile): VirtualFileWithValidationResult =
        VirtualFileWithValidationResult(virtualFile, this)
}

data class VirtualFileWithValidationResult(val virtualFile: VirtualFile, val validationResult: ValidationResult)
