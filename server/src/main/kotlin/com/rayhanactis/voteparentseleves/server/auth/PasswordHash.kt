package com.rayhanactis.voteparentseleves.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHash {
    private const val ALGO = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private val random = SecureRandom()

    fun hacher(motDePasse: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { random.nextBytes(it) }
        val cle = deriver(motDePasse, salt)
        return encode(salt, cle)
    }

    fun verifier(motDePasse: String, hashStocke: String): Boolean {
        val (salt, attendu) = decode(hashStocke) ?: return false
        val obtenu = deriver(motDePasse, salt)
        return MessageDigest.isEqual(obtenu, attendu)
    }

    private fun deriver(motDePasse: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(motDePasse.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).encoded
    }

    private fun encode(salt: ByteArray, cle: ByteArray): String {
        val enc = Base64.getEncoder()
        return "${enc.encodeToString(salt)}:${enc.encodeToString(cle)}"
    }

    private fun decode(stocke: String): Pair<ByteArray, ByteArray>? {
        val parts = stocke.split(":")
        if (parts.size != 2) return null
        val dec = Base64.getDecoder()
        val salt = runCatching { dec.decode(parts[0]) }.getOrNull() ?: return null
        val cle = runCatching { dec.decode(parts[1]) }.getOrNull() ?: return null
        return salt to cle
    }
}
