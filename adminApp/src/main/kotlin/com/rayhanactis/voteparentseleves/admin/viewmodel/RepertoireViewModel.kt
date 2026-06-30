package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.CreationParent
import com.rayhanactis.voteparentseleves.admin.ResultatEnvoiMasse
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.Electeur
import kotlinx.coroutines.launch

class RepertoireViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatRepertoire>(EtatRepertoire.Chargement)
        private set

    var recherche by mutableStateOf("")

    var etatEnvoiTous by mutableStateOf<EtatEnvoiTous>(EtatEnvoiTous.Inactif)
        private set

    fun envoyerATous() {
        if (etatEnvoiTous is EtatEnvoiTous.EnCours) return
        etatEnvoiTous = EtatEnvoiTous.EnCours
        viewModelScope.launch {
            etatEnvoiTous = when (val r = api.envoyerIdentifiantsTous(token)) {
                is ApiResult.Succes -> EtatEnvoiTous.Termine(r.data)
                is ApiResult.Echec -> EtatEnvoiTous.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatEnvoiTous.Erreur(r.messageLisible())
            }
        }
    }

    fun fermerEnvoiTous() {
        etatEnvoiTous = EtatEnvoiTous.Inactif
    }

    fun charger() {
        etat = EtatRepertoire.Chargement
        viewModelScope.launch {
            etat = when (val r = api.listerParents(token)) {
                is ApiResult.Succes -> EtatRepertoire.Pret(r.data)
                is ApiResult.Echec -> EtatRepertoire.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatRepertoire.Erreur(r.messageLisible())
            }
        }
    }

    fun creerParent(nom: String, prenom: String, email: String, onSucces: (Electeur) -> Unit) {
        if (nom.isBlank() || prenom.isBlank()) return
        viewModelScope.launch {
            val r = api.creerParent(token, CreationParent(nom.trim(), prenom.trim(), email.trim()))
            if (r is ApiResult.Succes) {
                onSucces(r.data)
                charger()
            } else {
                etat = EtatRepertoire.Erreur(
                    (r as? ApiResult.Echec)?.messageLisible()
                        ?: (r as? ApiResult.Reseau)?.messageLisible()
                        ?: "Erreur"
                )
            }
        }
    }

    fun supprimerParent(parentId: String) {
        viewModelScope.launch {
            val r = api.supprimerParent(token, parentId)
            if (r is ApiResult.Succes) charger()
            else etat = EtatRepertoire.Erreur(
                (r as? ApiResult.Echec)?.messageLisible()
                    ?: (r as? ApiResult.Reseau)?.messageLisible()
                    ?: "Erreur"
            )
        }
    }

    fun parentsFiltres(): List<Electeur> {
        val data = (etat as? EtatRepertoire.Pret)?.parents ?: return emptyList()
        val q = recherche.trim().lowercase()
        if (q.isBlank()) return data
        return data.filter { p ->
            p.nom.lowercase().contains(q) ||
                p.prenom.lowercase().contains(q) ||
                p.id.lowercase().contains(q) ||
                p.email.lowercase().contains(q)
        }
    }
}

sealed class EtatRepertoire {
    data object Chargement : EtatRepertoire()
    data class Pret(val parents: List<Electeur>) : EtatRepertoire()
    data class Erreur(val message: String) : EtatRepertoire()
}

sealed class EtatEnvoiTous {
    data object Inactif : EtatEnvoiTous()
    data object EnCours : EtatEnvoiTous()
    data class Termine(val bilan: ResultatEnvoiMasse) : EtatEnvoiTous()
    data class Erreur(val message: String) : EtatEnvoiTous()
}
