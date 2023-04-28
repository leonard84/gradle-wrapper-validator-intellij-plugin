package org.gradle.intellij.plugin.gradlewrappervalidator.services

import org.gradle.intellij.plugin.gradlewrappervalidator.domain.Sha256
import java.io.InputStream
import java.security.MessageDigest

object Hasher {
    private val HEX_CHARS = "0123456789abcdef".toCharArray()
    fun sha256(input: InputStream): Sha256 {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        val hashBytes = digest.digest()
        val result = StringBuilder(hashBytes.size * 2)

        hashBytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return Sha256(result.toString())
    }
}
