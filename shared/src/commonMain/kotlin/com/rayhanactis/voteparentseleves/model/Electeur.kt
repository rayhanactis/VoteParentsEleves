package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class Electeur(
    val id: String,
    val nom: String,
    val prenom: String,
    val ecoleId: String,
    val email: String = "",
    val aVote: Boolean = false
)
