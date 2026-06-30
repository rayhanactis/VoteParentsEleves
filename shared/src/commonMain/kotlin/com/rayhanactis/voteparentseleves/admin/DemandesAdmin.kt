package com.rayhanactis.voteparentseleves.admin

import kotlinx.serialization.Serializable

@Serializable
data class CreationScrutin(
    val ecoleId: String,
    val dateDebut: Long,
    val dateFin: Long,
    val nbSieges: Int,
    val nom: String = ""
)

@Serializable
data class RenommageScrutin(
    val nom: String
)

@Serializable
data class CreationListe(
    val nom: String,
    val candidats: List<CreationCandidat>,
    val slogan: String = "",
    val description: String = ""
)

@Serializable
data class CreationCandidat(
    val nom: String,
    val prenom: String,
    val electeurId: String? = null
)

@Serializable
data class CreationParent(
    val nom: String,
    val prenom: String,
    val email: String = ""
)

@Serializable
data class ModificationParent(
    val nom: String,
    val prenom: String,
    val email: String = ""
)

@Serializable
data class MiseAJourParametres(
    val nomEcole: String,
    val codePostal: String,
    val codeEcole: String,
    val emailExpediteur: String = "",
    val smtpHost: String = "",
    val smtpPort: Int = 587,
    val smtpMotDePasse: String = ""
)

@Serializable
data class IdentifiantsGeneres(
    val identifiant: String,
    val motDePasseClair: String
)

@Serializable
data class ResultatEnvoiMasse(
    val envoyes: Int,
    val sansEmail: Int,
    val echecs: Int
)

@Serializable
data class CreationElecteur(
    val id: String,
    val nom: String,
    val prenom: String,
    val ecoleId: String
)

@Serializable
data class ImportElecteurs(
    val electeurs: List<CreationElecteur>
)

@Serializable
data class ResultatImport(
    val ajoutes: Int,
    val ignores: Int
)

@Serializable
data class LigneElecteurBrute(
    val nom: String,
    val prenom: String
)

@Serializable
data class GenerationElecteurs(
    val electeurs: List<LigneElecteurBrute>
)

@Serializable
data class ElecteurGenere(
    val id: String,
    val nom: String,
    val prenom: String,
    val motDePasseClair: String,
    val ecoleId: String
)
