package com.rayhanactis.voteparentseleves.ui.ecrans

import com.rayhanactis.voteparentseleves.model.Scrutin

sealed class EtapeAcces {
    data object Decouverte : EtapeAcces()
    data class Chargement(val baseUrl: String) : EtapeAcces()
    data class ChoixScrutin(val baseUrl: String, val scrutins: List<Scrutin>) : EtapeAcces()
    data class SalleAttente(val baseUrl: String) : EtapeAcces()
    data class Erreur(val baseUrl: String, val message: String) : EtapeAcces()
    data class PretAVoter(val baseUrl: String, val scrutinId: String) : EtapeAcces()
}
