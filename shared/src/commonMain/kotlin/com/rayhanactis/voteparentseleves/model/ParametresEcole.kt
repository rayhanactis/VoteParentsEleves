package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

/**
 * Paramètres de l'établissement scolaire utilisateur de l'app.
 * Une seule ligne en BDD ; l'admin les configure depuis l'écran « Paramètres ».
 * Le `codeEcole` est l'identifiant technique utilisé partout (scrutins, parents)
 * pour remplir automatiquement les `ecoleId` sans demander à l'admin.
 */
@Serializable
data class ParametresEcole(
    val nomEcole: String,
    val codePostal: String,
    val codeEcole: String,
    // Configuration SMTP pour l'envoi des identifiants aux parents par email.
    val emailExpediteur: String = "",
    val smtpHost: String = "",
    val smtpPort: Int = 587,
    val smtpMotDePasse: String = ""
)
