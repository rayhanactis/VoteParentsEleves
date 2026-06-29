package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class ResultatScrutin(
    val scrutinId: String,
    val resultatsParListe: Map<String, Int>,
    val siegesAttribues: Map<String, Int>,
    val procesVerbal: String
)
