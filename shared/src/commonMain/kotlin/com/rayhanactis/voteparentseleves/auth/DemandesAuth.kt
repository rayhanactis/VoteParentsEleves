package com.rayhanactis.voteparentseleves.auth

import kotlinx.serialization.Serializable

@Serializable
data class DemandeLoginElecteur(
    val code: String,
    val motDePasse: String,
    val scrutinId: String
)

@Serializable
data class DemandeLoginAdmin(
    val code: String,
    val motDePasse: String
)

@Serializable
data class ReponseToken(
    val token: String
)
