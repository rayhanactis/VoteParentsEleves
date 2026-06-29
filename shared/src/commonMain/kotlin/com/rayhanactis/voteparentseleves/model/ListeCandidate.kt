package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class ListeCandidate(
    val id: String,
    val nom: String,
    val candidats: List<Candidat>,
    val scrutinId: String,
    val slogan: String = "",
    val description: String = ""
)
