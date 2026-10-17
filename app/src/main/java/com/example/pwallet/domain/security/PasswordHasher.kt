package com.example.pwallet.domain.security


import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class HashedPassword(val hash: String, val salt: String, val iterations: Int)

class PasswordHasher(private val iterations: Int = DEFAULT_ITERATIONS) {

    private val random = SecureRandom()

    fun hash(password: String): HashedPassword {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        return HashedPassword(derive(password, salt, iterations).toHex(), salt.toHex(), iterations)
    }

    fun verify(password: String, stored: HashedPassword): Boolean {
        val actual = derive(password, stored.salt.fromHex(), stored.iterations)
        return MessageDigest.isEqual(actual, stored.hash.fromHex())
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.fromHex() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        const val DEFAULT_ITERATIONS = 210_000
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
    }
}