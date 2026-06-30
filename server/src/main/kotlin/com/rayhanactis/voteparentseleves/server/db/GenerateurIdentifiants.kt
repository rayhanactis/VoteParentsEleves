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

fun genererIdentifiantUnique(prenom: String, nom: String, dejaUtilises: Set<String>): String {
    val base = "${prenom.versSlug()}.${nom.versSlug()}".trim('.').ifBlank { "parent" }
    if (base !in dejaUtilises) return base
    var compteur = 2
    while ("$base$compteur" in dejaUtilises) compteur++
    return "$base$compteur"
}

private val random = SecureRandom()

fun genererMotDePasseLisible(): String = random.nextInt(1_000_000).toString().padStart(6, '0')
