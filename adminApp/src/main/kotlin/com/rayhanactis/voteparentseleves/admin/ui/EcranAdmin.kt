package com.rayhanactis.voteparentseleves.admin.ui

import com.rayhanactis.voteparentseleves.model.ListeCandidate

sealed class EcranAdmin {
    data object Login : EcranAdmin()
    data class Dashboard(val token: String) : EcranAdmin()
    data class CreationScrutin(val token: String) : EcranAdmin()
    data class DetailScrutin(val token: String, val scrutinId: String) : EcranAdmin()
    data class Resultats(val token: String, val scrutinId: String) : EcranAdmin()
    data class CreationListe(val token: String, val scrutinId: String) : EcranAdmin()
    data class ModificationListe(
        val token: String,
        val scrutinId: String,
        val liste: ListeCandidate
    ) : EcranAdmin()
    data class Repertoire(val token: String) : EcranAdmin()
    data class Assistant(val token: String) : EcranAdmin()
    data class DetailParent(val token: String, val parentId: String) : EcranAdmin()
    data class Parametres(val token: String) : EcranAdmin()
    data class ProjectionQr(val retourVers: EcranAdmin) : EcranAdmin()
}
