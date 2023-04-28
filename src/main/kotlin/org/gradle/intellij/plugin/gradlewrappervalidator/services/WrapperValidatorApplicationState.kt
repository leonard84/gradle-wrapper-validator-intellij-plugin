package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.OptionTag
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Sha256
import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Version
import java.time.Instant

data class WrapperValidatorApplicationState(
    // configuration
    var activateValidationByDefault: Boolean = true,
    var showAlertBoxOnValidationFailure: Boolean = true,
    var renameWrapperOnValidationFailure: Boolean = false,
    //--- internal
    var etagOfLastUpdate: String? = null,
    @OptionTag(converter = InstantConverter::class)
    var lastUpdate: Instant? = null,
    var storedWrapperHashes: MutableMap<String, String> = mutableMapOf()

) {
    val wrapperHashes: Map<Version, Sha256>
        // We use inline classes for Version and Sha256, but XML serialization doesn't support them.
        get() = storedWrapperHashes.entries.associateTo(mutableMapOf()) { Version(it.key) to Sha256(it.value) }

    val wrapperHashToVersions: Map<Sha256, List<Version>>
        get() = storedWrapperHashes.entries.groupBy({ Sha256(it.value) }, { Version(it.key) })

    fun addNewHashes(newHashes: Map<Version, Sha256>) {
        newHashes.entries.forEach { storedWrapperHashes.put(it.key.stringValue, it.value.stringValue) }
    }
}

class InstantConverter : Converter<Instant>() {
    override fun toString(value: Instant): String = value.toString()

    override fun fromString(value: String): Instant = Instant.parse(value)
}
