package com.rayhanactis.voteparentseleves.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import kotlinx.coroutines.launch

class ListesViewModel(private val api: ApiClient) : ViewModel() {

    var etat by mutableStateOf<EtatListes>(EtatListes.Chargement)
        private set

    fun charger(scrutinId: String) {
        etat = EtatListes.Chargement
        viewModelScope.launch {
            etat = when (val r = api.listerListes(scrutinId)) {
                is ApiResult.Succes -> EtatListes.Pretes(r.data)
                is ApiResult.Echec -> EtatListes.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatListes.Erreur(r.messageLisible())
            }
        }
    }
}

sealed class EtatListes {
    data object Chargement : EtatListes()
    data class Pretes(val listes: List<ListeCandidate>) : EtatListes()
    data class Erreur(val message: String) : EtatListes()
}
