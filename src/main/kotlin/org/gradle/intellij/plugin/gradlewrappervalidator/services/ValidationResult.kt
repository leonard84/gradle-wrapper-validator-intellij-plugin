package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.openapi.vfs.VirtualFile
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Sha256
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Version

sealed class ValidationResult {
    /* Indicates that a validation could not be performed, e.g., file was already deleted. */
    object NotApplicable : ValidationResult()
    data class Invalid(val invalidHash: Sha256) : ValidationResult()
    data class Valid(val versions: List<Version>) : ValidationResult()

    companion object {
        fun valid(versions: List<Version>): ValidationResult = Valid(versions)

        fun invalid(invalidHash: Sha256): ValidationResult = Invalid(invalidHash)
    }

    fun withVirtualFile(virtualFile: VirtualFile): VirtualFileWithValidationResult =
        VirtualFileWithValidationResult(virtualFile, this)
}

data class VirtualFileWithValidationResult(val virtualFile: VirtualFile, val validationResult: ValidationResult)
