package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.ModificationParent
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.Electeur
import kotlinx.coroutines.launch

class DetailParentViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatDetailParent>(EtatDetailParent.Chargement)
        private set

    // Mot de passe en clair généré pendant cette session (non restocké côté
    // serveur : seulement le hash). Null = aucun mot de passe affichable.
    var motDePasseGenere by mutableStateOf<String?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var actionEnCours by mutableStateOf(false)
        private set

    fun charger(parentId: String) {
        etat = EtatDetailParent.Chargement
        motDePasseGenere = null
        message = null
        viewModelScope.launch {
            etat = when (val r = api.lireParent(token, parentId)) {
                is ApiResult.Succes -> EtatDetailParent.Pret(r.data)
                is ApiResult.Echec -> EtatDetailParent.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatDetailParent.Erreur(r.messageLisible())
            }
        }
    }

    fun modifier(parentId: String, nom: String, prenom: String, email: String) {
        viewModelScope.launch {
            val r = api.modifierParent(
                token = token,
                parentId = parentId,
                req = ModificationParent(nom.trim(), prenom.trim(), email.trim())
            )
            when (r) {
                is ApiResult.Succes -> {
                    etat = EtatDetailParent.Pret(r.data)
                    message = "Informations enregistrées."
                }
                is ApiResult.Echec -> message = r.messageLisible()
                is ApiResult.Reseau -> message = r.messageLisible()
            }
        }
    }

    fun genererMotDePasse(parentId: String) {
        if (actionEnCours) return
        actionEnCours = true
        message = null
        viewModelScope.launch {
            when (val r = api.genererMotDePasseParent(token, parentId)) {
                is ApiResult.Succes -> {
                    motDePasseGenere = r.data.motDePasseClair
                    message = "Mot de passe généré. Notez-le ou envoyez-le : il ne sera plus affichable après."
                }
                is ApiResult.Echec -> message = r.messageLisible()
                is ApiResult.Reseau -> message = r.messageLisible()
            }
            actionEnCours = false
        }
    }

    fun envoyerParMail(parentId: String) {
        if (actionEnCours) return
        actionEnCours = true
        message = null
        viewModelScope.launch {
            when (val r = api.envoyerIdentifiants(token, parentId)) {
                is ApiResult.Succes -> {
                    motDePasseGenere = r.data.motDePasseClair
                    message = "Identifiants envoyés par email."
                }
                is ApiResult.Echec -> message = r.messageLisible()
                is ApiResult.Reseau -> message = r.messageLisible()
            }
            actionEnCours = false
        }
    }

    fun effacerMessage() { message = null }
}

sealed class EtatDetailParent {
    data object Chargement : EtatDetailParent()
    data class Pret(val parent: Electeur) : EtatDetailParent()
    data class Erreur(val message: String) : EtatDetailParent()
}
