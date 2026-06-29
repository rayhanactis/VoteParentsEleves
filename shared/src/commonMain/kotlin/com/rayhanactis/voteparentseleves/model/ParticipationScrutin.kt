package com.rayhanactis.voteparentseleves.model

import kotlinx.serialization.Serializable

// Suivi de participation en temps réel pendant qu'un scrutin est ouvert.
// N'expose aucun choix de vote (anonymat) : uniquement des compteurs.
@Serializable
data class ParticipationScrutin(
    val scrutinId: String,
    val totalElecteurs: Int,
    val nbVotants: Int,
    // Horodatage (epoch millis) du dernier vote déposé, null si aucun vote.
    val dernierVote: Long? = null
) {
    // Taux dans [0, 1]. Propriété calculée : non sérialisée.
    val tauxParticipation: Float
        get() = if (totalElecteurs <= 0) 0f else nbVotants.toFloat() / totalElecteurs
}
