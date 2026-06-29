package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class Scrutin(
    val id: String,
    val ecoleId: String,
    val nom: String = "",
    val dateDebut: Long,
    val dateFin: Long,
    val nbSieges: Int,
    val statut: StatutScrutin
)
