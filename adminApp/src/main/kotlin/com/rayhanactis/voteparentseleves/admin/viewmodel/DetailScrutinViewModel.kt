package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.ParticipationScrutin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import kotlinx.coroutines.launch

class DetailScrutinViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatDetail>(EtatDetail.Chargement)
        private set

    var actionEnCours by mutableStateOf(false)
        private set

    var participation by mutableStateOf<ParticipationScrutin?>(null)
        private set

    suspend fun rafraichirParticipation(scrutinId: String) {
        when (val r = api.participation(token, scrutinId)) {
            is ApiResult.Succes -> participation = r.data
            is ApiResult.Echec -> Unit
            is ApiResult.Reseau -> Unit
        }
    }

    fun charger(scrutinId: String) {
        etat = EtatDetail.Chargement
        viewModelScope.launch {
            val scrutin = api.lireScrutin(scrutinId)
            val listes = api.listerListes(scrutinId)
            val resultats = if (scrutin is ApiResult.Succes && scrutin.data.statut.aDesResultatsAAfficher()) {
                api.lireResultats(scrutinId)
            } else null
            etat = construireEtat(scrutin, listes, resultats)
        }
    }

    fun ouvrir(scrutinId: String) = avecAction(scrutinId) {
        api.ouvrirScrutin(token, scrutinId)
    }

    fun fermer(scrutinId: String) = avecAction(scrutinId) {
        api.fermerScrutin(token, scrutinId)
    }

    fun depouiller(scrutinId: String) = avecAction(scrutinId) {
        api.depouillerScrutin(token, scrutinId)
    }

    fun renommer(scrutinId: String, nouveauNom: String) = avecAction(scrutinId) {
        api.renommerScrutin(token, scrutinId, nouveauNom)
    }

    fun supprimerScrutin(scrutinId: String, onSupprime: () -> Unit) {
        if (actionEnCours) return
        actionEnCours = true
        viewModelScope.launch {
            val r = api.supprimerScrutin(token, scrutinId)
            actionEnCours = false
            when (r) {
                is ApiResult.Succes -> onSupprime()
                is ApiResult.Echec -> etat = EtatDetail.Erreur(r.messageLisible())
                is ApiResult.Reseau -> etat = EtatDetail.Erreur(r.messageLisible())
            }
        }
    }

    fun supprimerListe(scrutinId: String, listeId: String) {
        if (actionEnCours) return
        actionEnCours = true
        viewModelScope.launch {
            val r = api.supprimerListe(token, scrutinId, listeId)
            actionEnCours = false
            when (r) {
                is ApiResult.Succes -> charger(scrutinId)
                is ApiResult.Echec -> etat = EtatDetail.Erreur(r.messageLisible())
                is ApiResult.Reseau -> etat = EtatDetail.Erreur(r.messageLisible())
            }
        }
    }

    private fun avecAction(scrutinId: String, appel: suspend () -> ApiResult<Scrutin>) {
        if (actionEnCours) return
        actionEnCours = true
        viewModelScope.launch {
            val r = appel()
            actionEnCours = false
            when (r) {
                is ApiResult.Succes -> charger(scrutinId)
                is ApiResult.Echec -> etat = EtatDetail.Erreur(r.messageLisible())
                is ApiResult.Reseau -> etat = EtatDetail.Erreur(r.messageLisible())
            }
        }
    }

    private fun construireEtat(
        scrutin: ApiResult<Scrutin>,
        listes: ApiResult<List<ListeCandidate>>,
        resultats: ApiResult<ResultatScrutin>?
    ): EtatDetail = when {
        scrutin is ApiResult.Succes && listes is ApiResult.Succes ->
            EtatDetail.Pret(scrutin.data, listes.data, (resultats as? ApiResult.Succes)?.data)
        scrutin is ApiResult.Echec -> EtatDetail.Erreur(scrutin.messageLisible())
        scrutin is ApiResult.Reseau -> EtatDetail.Erreur(scrutin.messageLisible())
        listes is ApiResult.Echec -> EtatDetail.Erreur(listes.messageLisible())
        listes is ApiResult.Reseau -> EtatDetail.Erreur(listes.messageLisible())
        else -> EtatDetail.Erreur("Erreur inconnue")
    }
}

private fun StatutScrutin.aDesResultatsAAfficher(): Boolean =
    this == StatutScrutin.Ferme || this == StatutScrutin.Depouille

sealed class EtatDetail {
    data object Chargement : EtatDetail()
    data class Pret(
        val scrutin: Scrutin,
        val listes: List<ListeCandidate>,
        val resultats: ResultatScrutin? = null
    ) : EtatDetail()
    data class Erreur(val message: String) : EtatDetail()
}
