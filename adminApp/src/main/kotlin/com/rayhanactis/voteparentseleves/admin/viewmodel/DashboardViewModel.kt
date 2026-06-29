package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.Scrutin
import kotlinx.coroutines.launch

class DashboardViewModel(private val api: ApiClient) : ViewModel() {

    var etat by mutableStateOf<EtatDashboard>(EtatDashboard.Chargement)
        private set

    fun charger() {
        etat = EtatDashboard.Chargement
        viewModelScope.launch {
            etat = when (val r = api.listerScrutins()) {
                is ApiResult.Succes -> EtatDashboard.Pret(r.data)
                is ApiResult.Echec -> EtatDashboard.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatDashboard.Erreur(r.messageLisible())
            }
        }
    }
}

sealed class EtatDashboard {
    data object Chargement : EtatDashboard()
    data class Pret(val scrutins: List<Scrutin>) : EtatDashboard()
    data class Erreur(val message: String) : EtatDashboard()
}
