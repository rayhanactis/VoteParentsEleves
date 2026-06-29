package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class Candidat(
    val id: String,
    val nom: String,
    val prenom: String,
    val listeCandidateId: String
)
