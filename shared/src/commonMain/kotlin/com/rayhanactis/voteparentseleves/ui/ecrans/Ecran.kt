package com.rayhanactis.voteparentseleves.ui.ecrans

import com.rayhanactis.voteparentseleves.model.ListeCandidate

sealed class Ecran {
    data object Login : Ecran()
    data class ListesPresentation(val token: String, val scrutinId: String) : Ecran()
    data class Vote(
        val token: String,
        val scrutinId: String,
        val listes: List<ListeCandidate>
    ) : Ecran()
    data class Confirmation(
        val token: String,
        val scrutinId: String,
        val listes: List<ListeCandidate>,
        val choix: ChoixVote
    ) : Ecran()
    data class Recu(val bulletinId: String, val nomListe: String?) : Ecran()
    // L'électeur a déjà voté pour ce scrutin : écran de remerciement.
    data object DejaVote : Ecran()
}

sealed class ChoixVote {
    data class PourListe(val listeId: String, val nomListe: String) : ChoixVote()
    data object Blanc : ChoixVote()

    fun libelle(): String = when (this) {
        is PourListe -> nomListe
        Blanc -> "Vote blanc"
    }
}
