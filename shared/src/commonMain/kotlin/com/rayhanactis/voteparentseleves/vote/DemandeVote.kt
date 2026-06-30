package com.rayhanactis.voteparentseleves.vote

import kotlinx.serialization.Serializable

@Serializable
data class DemandeVote(
    val listeCandidateId: String? = null
)

@Serializable
data class RecuVote(
    val bulletinId: String,
    val scrutinId: String
)
