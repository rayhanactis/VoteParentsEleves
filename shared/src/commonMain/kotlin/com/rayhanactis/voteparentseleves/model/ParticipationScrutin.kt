package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

@Serializable
data class ParticipationScrutin(
    val scrutinId: String,
    val totalElecteurs: Int,
    val nbVotants: Int,
    val dernierVote: Long? = null
) {
    val tauxParticipation: Float
        get() = if (totalElecteurs <= 0) 0f else nbVotants.toFloat() / totalElecteurs
}
