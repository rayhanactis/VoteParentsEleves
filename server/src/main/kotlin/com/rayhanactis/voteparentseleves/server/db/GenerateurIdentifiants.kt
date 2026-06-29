package com.rayhanactis.voteparentseleves.server.db

import java.security.SecureRandom

private val ACCENTS = mapOf(
    'à' to 'a', 'â' to 'a', 'ä' to 'a',
    'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
    'î' to 'i', 'ï' to 'i',
    'ô' to 'o', 'ö' to 'o',
    'ù' to 'u', 'û' to 'u', 'ü' to 'u',
    'ç' to 'c', 'ñ' to 'n'
)

private fun String.versSlug(): String = lowercase()
    .map { ACCENTS[it] ?: it }
    .joinToString("")
    .filter { it.isLetterOrDigit() || it == ' ' || it == '-' }
    .trim()
    .replace(Regex("\\s+"), "-")

// Identifiant lisible "prenom.nom" (suffixe numérique si collision). Servira
// de code de connexion électeur, donc doit rester simple à retaper à la main
// même s'il a aussi vocation à être scanné via QR code.
fun genererIdentifiantUnique(prenom: String, nom: String, dejaUtilises: Set<String>): String {
    val base = "${prenom.versSlug()}.${nom.versSlug()}".trim('.').ifBlank { "parent" }
    if (base !in dejaUtilises) return base
    var compteur = 2
    while ("$base$compteur" in dejaUtilises) compteur++
    return "$base$compteur"
}

private val random = SecureRandom()

// PIN à 6 chiffres : assez simple à retaper pour un parent non technique,
// assez d'entropie (10^6) face à 5 essais/min imposés par le rate limit.
fun genererMotDePasseLisible(): String = random.nextInt(1_000_000).toString().padStart(6, '0')
