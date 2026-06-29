package com.rayhanactis.voteparentseleves.vote

import kotlinx.serialization.Serializable

@Serializable
data class DemandeVote(
    val listeCandidateId: String? = null // null = vote blanc
)

@Serializable
data class RecuVote(
    val bulletinId: String,
    val scrutinId: String
)
