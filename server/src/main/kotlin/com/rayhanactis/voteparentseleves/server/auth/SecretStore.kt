package com.rayhanactis.voteparentseleves.server.auth

import java.io.File
import java.security.SecureRandom
import java.util.Base64

// Génère un secret JWT aléatoire au premier démarrage et le persiste dans un
// fichier local (un par poste école), pour ne plus dépendre d'un secret en
// dur dans le code. Les démarrages suivants relisent le même fichier.
object SecretStore {
    private val random = SecureRandom()

    fun chargerOuCreer(fichier: File): String {
        val existant = runCatching { fichier.readText().trim() }.getOrNull()
        if (!existant.isNullOrBlank()) return existant

        val secret = genererSecret()
        fichier.parentFile?.mkdirs()
        fichier.writeText(secret)
        return secret
    }

    private fun genererSecret(): String {
        val octets = ByteArray(64).also { random.nextBytes(it) }
        return Base64.getEncoder().encodeToString(octets)
    }
}

fun cheminSecretParDefaut(): File =
    File(System.getProperty("user.home"), ".voteparentseleves/jwt-secret.key")
