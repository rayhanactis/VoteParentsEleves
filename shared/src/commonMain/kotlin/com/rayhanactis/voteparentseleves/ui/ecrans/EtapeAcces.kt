package com.rayhanactis.voteparentseleves.ui.ecrans

import com.rayhanactis.voteparentseleves.model.Scrutin

// État de la phase "avant de pouvoir voter" : découverte du serveur école
// (par QR ou saisie manuelle), puis sélection du scrutin ouvert si la
// découverte n'a pas pré-rempli un scrutin précis (cf. ROADMAP Phase 4).
sealed class EtapeAcces {
    data object Decouverte : EtapeAcces()
    data class Chargement(val baseUrl: String) : EtapeAcces()
    data class ChoixScrutin(val baseUrl: String, val scrutins: List<Scrutin>) : EtapeAcces()
    // Connecté au serveur de l'école mais aucun scrutin n'est encore ouvert :
    // salle d'attente qui se débloque automatiquement à l'ouverture.
    data class SalleAttente(val baseUrl: String) : EtapeAcces()
    data class Erreur(val baseUrl: String, val message: String) : EtapeAcces()
    data class PretAVoter(val baseUrl: String, val scrutinId: String) : EtapeAcces()
}
