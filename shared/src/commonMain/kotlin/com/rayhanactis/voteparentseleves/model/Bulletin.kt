package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class Bulletin(
    val id: String,
    val scrutinId: String,
    val listeCandidateId: String? = null // null = vote blanc
)
