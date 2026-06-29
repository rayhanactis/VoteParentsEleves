package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.CreationScrutin as CreationScrutinDto
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.model.Scrutin
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

class CreationScrutinViewModel(
    private val api: ApiClient,
    private val token: String
) : ViewModel() {

    var etat by mutableStateOf<EtatCreationScrutin>(EtatCreationScrutin.Saisie)
        private set

    fun creer(
        nom: String,
        dateDebut: LocalDateTime,
        dateFin: LocalDateTime,
        nbSieges: Int,
        onSucces: (Scrutin) -> Unit
    ) {
        if (nom.isBlank()) {
            etat = EtatCreationScrutin.Erreur("Le nom du scrutin est obligatoire.")
            return
        }
        if (!dateDebut.isBefore(dateFin)) {
            etat = EtatCreationScrutin.Erreur("La date de fin doit être après la date de début.")
            return
        }
        if (nbSieges < 1) {
            etat = EtatCreationScrutin.Erreur("Le nombre de sièges doit être supérieur à 0.")
            return
        }
        val zone = ZoneId.systemDefault()
        val debutMs = dateDebut.atZone(zone).toEpochSecond() * 1000L
        val finMs = dateFin.atZone(zone).toEpochSecond() * 1000L

        etat = EtatCreationScrutin.Envoi
        viewModelScope.launch {
            val r = api.creerScrutin(
                token,
                CreationScrutinDto(
                    // ecoleId vide → le serveur prend celui des Paramètres établissement
                    ecoleId = "",
                    nom = nom.trim(),
                    dateDebut = debutMs,
                    dateFin = finMs,
                    nbSieges = nbSieges
                )
            )
            etat = when (r) {
                is ApiResult.Succes -> {
                    onSucces(r.data)
                    EtatCreationScrutin.Saisie
                }
                is ApiResult.Echec -> EtatCreationScrutin.Erreur(r.messageLisible())
                is ApiResult.Reseau -> EtatCreationScrutin.Erreur(r.messageLisible())
            }
        }
    }

    fun resetErreur() {
        if (etat is EtatCreationScrutin.Erreur) etat = EtatCreationScrutin.Saisie
    }
}

sealed class EtatCreationScrutin {
    data object Saisie : EtatCreationScrutin()
    data object Envoi : EtatCreationScrutin()
    data class Erreur(val message: String) : EtatCreationScrutin()
}
