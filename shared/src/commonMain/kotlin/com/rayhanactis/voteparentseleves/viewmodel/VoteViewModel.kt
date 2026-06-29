package com.rayhanactis.voteparentseleves.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.ui.ecrans.ChoixVote
import com.rayhanactis.voteparentseleves.vote.DemandeVote
import com.rayhanactis.voteparentseleves.vote.RecuVote
import kotlinx.coroutines.launch

class VoteViewModel(private val api: ApiClient) : ViewModel() {

    var etat by mutableStateOf<EtatVote>(EtatVote.Pret)
        private set

    fun deposer(
        token: String,
        scrutinId: String,
        choix: ChoixVote,
        onSucces: (RecuVote) -> Unit
    ) {
        etat = EtatVote.Envoi
        val listeId = when (choix) {
            is ChoixVote.PourListe -> choix.listeId
            ChoixVote.Blanc -> null
        }
        viewModelScope.launch {
            etat = when (val r = api.voter(token, scrutinId, DemandeVote(listeId))) {
                is ApiResult.Succes -> {
                    onSucces(r.data)
                    EtatVote.Pret
                }
                is ApiResult.Echec -> EtatVote.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatVote.Erreur(r.messageLisible())
            }
        }
    }

    fun resetErreur() {
        if (etat is EtatVote.Erreur) etat = EtatVote.Pret
    }
}

sealed class EtatVote {
    data object Pret : EtatVote()
    data object Envoi : EtatVote()
    data class Erreur(val message: String) : EtatVote()
}
