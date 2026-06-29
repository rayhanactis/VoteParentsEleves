package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.MiseAJourParametres
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.ParametresEcole
import kotlinx.coroutines.launch

class ParametresViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatParametres>(EtatParametres.Chargement)
        private set

    var messageSauvegarde by mutableStateOf<String?>(null)
        private set

    fun charger() {
        etat = EtatParametres.Chargement
        viewModelScope.launch {
            etat = when (val r = api.lireParametres(token)) {
                is ApiResult.Succes -> EtatParametres.Pret(r.data)
                is ApiResult.Echec -> EtatParametres.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatParametres.Erreur(r.messageLisible())
            }
        }
    }

    fun enregistrer(
        nomEcole: String,
        codePostal: String,
        codeEcole: String,
        emailExpediteur: String,
        smtpHost: String,
        smtpPort: Int,
        smtpMotDePasse: String
    ) {
        viewModelScope.launch {
            val r = api.enregistrerParametres(
                token = token,
                req = MiseAJourParametres(
                    nomEcole = nomEcole.trim(),
                    codePostal = codePostal.trim(),
                    codeEcole = codeEcole.trim(),
                    emailExpediteur = emailExpediteur.trim(),
                    smtpHost = smtpHost.trim(),
                    smtpPort = smtpPort,
                    smtpMotDePasse = smtpMotDePasse
                )
            )
            when (r) {
                is ApiResult.Succes -> {
                    etat = EtatParametres.Pret(r.data)
                    messageSauvegarde = "Paramètres enregistrés"
                }
                is ApiResult.Echec -> etat = EtatParametres.Erreur(r.messageLisible())
                is ApiResult.Reseau -> etat = EtatParametres.Erreur(r.messageLisible())
            }
        }
    }

    fun effacerMessage() { messageSauvegarde = null }
}

sealed class EtatParametres {
    data object Chargement : EtatParametres()
    data class Pret(val parametres: ParametresEcole) : EtatParametres()
    data class Erreur(val message: String) : EtatParametres()
}
