package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.CreationCandidat
import com.rayhanactis.voteparentseleves.admin.CreationListe as CreationListeDto
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import kotlinx.coroutines.launch

class CreationListeViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatCreationListe>(EtatCreationListe.Saisie)
        private set

    fun enregistrer(
        scrutinId: String,
        listeIdExistante: String?,
        nom: String,
        slogan: String,
        description: String,
        candidats: List<CreationCandidat>,
        onSucces: (ListeCandidate) -> Unit
    ) {
        if (nom.isBlank()) {
            etat = EtatCreationListe.Erreur("Le nom de la liste est obligatoire.")
            return
        }
        if (candidats.isEmpty()) {
            etat = EtatCreationListe.Erreur("Ajoutez au moins un candidat.")
            return
        }
        if (candidats.any { it.nom.isBlank() || it.prenom.isBlank() }) {
            etat = EtatCreationListe.Erreur("Chaque candidat doit avoir un nom et un prénom.")
            return
        }
        etat = EtatCreationListe.Envoi
        viewModelScope.launch {
            val req = CreationListeDto(
                nom = nom.trim(),
                candidats = candidats,
                slogan = slogan.trim(),
                description = description.trim()
            )
            val r = if (listeIdExistante == null) {
                api.ajouterListe(token, scrutinId, req)
            } else {
                api.modifierListe(token, scrutinId, listeIdExistante, req)
            }
            etat = when (r) {
                is ApiResult.Succes -> {
                    onSucces(r.data)
                    EtatCreationListe.Saisie
                }
                is ApiResult.Echec -> EtatCreationListe.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatCreationListe.Erreur(r.messageLisible())
            }
        }
    }

    fun resetErreur() {
        if (etat is EtatCreationListe.Erreur) etat = EtatCreationListe.Saisie
    }
}

sealed class EtatCreationListe {
    data object Saisie : EtatCreationListe()
    data object Envoi : EtatCreationListe()
    data class Erreur(val message: String) : EtatCreationListe()
}
