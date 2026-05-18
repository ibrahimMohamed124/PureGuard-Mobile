package com.pureguard.mobile.core.security

import android.annotation.TargetApi
import android.os.Build
import androidx.annotation.RequiresApi
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordRecord(
    val salt: String,
    val hash: String,
    val iterations: Int
)

class PasswordHasher {
    private val random = SecureRandom()

    @TargetApi(Build.VERSION_CODES.O)
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    fun create(password: String, iterations: Int = 150_000): PasswordRecord {
        val saltBytes = ByteArray(16).also(random::nextBytes)
        val hashBytes = hash(password, saltBytes, iterations)
        return PasswordRecord(
            salt = Base64.getEncoder().encodeToString(saltBytes),
            hash = Base64.getEncoder().encodeToString(hashBytes),
            iterations = iterations
        )
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(password: String, record: PasswordRecord): Boolean {
        val saltBytes = Base64.getDecoder().decode(record.salt)
        val computed = hash(password, saltBytes, record.iterations)
        val expected = Base64.getDecoder().decode(record.hash)
        if (expected.size != computed.size) return false
        var diff = 0
        for (i in expected.indices) {
            diff = diff or (expected[i].toInt() xor computed[i].toInt())
        }
        return diff == 0
    }

    private fun hash(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
