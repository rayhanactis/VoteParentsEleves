package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.auth.DemandeLoginAdmin
import kotlinx.coroutines.launch

class LoginAdminViewModel(private val api: ApiClient) : ViewModel() {

    var etat by mutableStateOf<EtatLoginAdmin>(EtatLoginAdmin.Pret)
        private set

    fun seConnecter(
        code: String,
        motDePasse: String,
        onSucces: (token: String) -> Unit
    ) {
        if (code.isBlank() || motDePasse.isBlank()) {
            etat = EtatLoginAdmin.Erreur("Veuillez saisir vos identifiants.")
            return
        }
        etat = EtatLoginAdmin.Chargement
        viewModelScope.launch {
            etat = when (val r = api.loginAdmin(DemandeLoginAdmin(code, motDePasse))) {
                is ApiResult.Succes -> EtatLoginAdmin.Pret.also { onSucces(r.data.token) }
                is ApiResult.Echec -> EtatLoginAdmin.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatLoginAdmin.Erreur(r.messageLisible())
            }
        }
    }

    fun resetErreur() {
        if (etat is EtatLoginAdmin.Erreur) etat = EtatLoginAdmin.Pret
    }
}

sealed class EtatLoginAdmin {
    data object Pret : EtatLoginAdmin()
    data object Chargement : EtatLoginAdmin()
    data class Erreur(val message: String) : EtatLoginAdmin()
}
