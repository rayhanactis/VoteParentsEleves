package com.rayhanactis.voteparentseleves.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.auth.DemandeLoginElecteur
import kotlinx.coroutines.launch

class LoginViewModel(private val api: ApiClient) : ViewModel() {

    var etat by mutableStateOf<EtatLogin>(EtatLogin.Pret)
        private set

    fun seConnecter(
        code: String,
        motDePasse: String,
        scrutinId: String,
        onSucces: (token: String, scrutinId: String, dejaVote: Boolean) -> Unit
    ) {
        if (code.isBlank() || motDePasse.isBlank()) {
            etat = EtatLogin.Erreur("Veuillez saisir votre code et votre mot de passe.")
            return
        }
        etat = EtatLogin.Chargement
        viewModelScope.launch {
            when (val r = api.loginElecteur(
                DemandeLoginElecteur(code = code, motDePasse = motDePasse, scrutinId = scrutinId)
            )) {
                is ApiResult.Succes -> {
                    val token = r.data.token
                    val dejaVote = (api.aDejaVote(token, scrutinId) as? ApiResult.Succes)?.data?.aVote ?: false
                    etat = EtatLogin.Pret
                    onSucces(token, scrutinId, dejaVote)
                }
                is ApiResult.Echec -> etat = EtatLogin.Erreur(r.messageLisible())
                is ApiResult.Reseau -> etat = EtatLogin.Erreur(r.messageLisible())
            }
        }
    }

    fun resetErreur() {
        if (etat is EtatLogin.Erreur) etat = EtatLogin.Pret
    }
}

sealed class EtatLogin {
    data object Pret : EtatLogin()
    data object Chargement : EtatLogin()
    data class Erreur(val message: String) : EtatLogin()
}
