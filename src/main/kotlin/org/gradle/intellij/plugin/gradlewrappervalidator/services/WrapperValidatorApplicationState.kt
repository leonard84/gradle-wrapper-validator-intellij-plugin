package org.gradle.intellij.plugin.gradlewrappervalidator.services

import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.OptionTag
import java.time.Instant

data class WrapperValidatorApplicationState(
    var etagOfLastUpdate: String? = null,
    @OptionTag(converter = InstantConverter::class)
    var lastUpdate: Instant? = null,
    val wrapperHashes: MutableMap<String, String> = mutableMapOf()
)

class InstantConverter : Converter<Instant>() {
    override fun toString(value: Instant): String = value.toString()

    override fun fromString(value: String): Instant = Instant.parse(value)
}
