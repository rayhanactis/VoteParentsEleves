package com.rayhanactis.voteparentseleves.admin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranCreationListe
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranCreationScrutin
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranDashboard
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranDetailParent
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranDetailScrutin
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranLoginAdmin
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranParametres
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranProjectionQr
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranAssistant
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranRepertoire
import com.rayhanactis.voteparentseleves.admin.ui.ecrans.EcranResultats
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.ui.theme.AppTheme

@Composable
fun AdminApp() {
    val api = remember { ApiClient(baseUrl = "http://localhost:8080") }

    CompositionLocalProvider(LocalApiClient provides api) {
        AppTheme {
            NavigationAdmin()
        }
    }
}

@Composable
private fun NavigationAdmin() {
    var ecran by remember { mutableStateOf<EcranAdmin>(EcranAdmin.Login) }
    when (val e = ecran) {
        EcranAdmin.Login -> EcranLoginAdmin(
            onConnecte = { token -> ecran = EcranAdmin.Dashboard(token) }
        )

        is EcranAdmin.Dashboard -> EcranDashboard(
            token = e.token,
            onDeconnexion = { ecran = EcranAdmin.Login },
            onNouveauScrutin = { ecran = EcranAdmin.CreationScrutin(e.token) },
            onSelectionnerScrutin = { scrutin ->
                ecran = EcranAdmin.DetailScrutin(e.token, scrutin.id)
            },
            onAllerRepertoire = { ecran = EcranAdmin.Repertoire(e.token) },
            onAllerParametres = { ecran = EcranAdmin.Parametres(e.token) },
            onAllerAssistant = { ecran = EcranAdmin.Assistant(e.token) },
            onProjeterQr = { ecran = EcranAdmin.ProjectionQr(retourVers = e) }
        )

        is EcranAdmin.Repertoire -> EcranRepertoire(
            token = e.token,
            onAllerDashboard = { ecran = EcranAdmin.Dashboard(e.token) },
            onAllerParametres = { ecran = EcranAdmin.Parametres(e.token) },
            onAllerAssistant = { ecran = EcranAdmin.Assistant(e.token) },
            onDeconnexion = { ecran = EcranAdmin.Login },
            onOuvrirParent = { parentId ->
                ecran = EcranAdmin.DetailParent(e.token, parentId)
            }
        )

        is EcranAdmin.DetailParent -> EcranDetailParent(
            token = e.token,
            parentId = e.parentId,
            onRetour = { ecran = EcranAdmin.Repertoire(e.token) }
        )

        is EcranAdmin.Parametres -> EcranParametres(
            token = e.token,
            onAllerDashboard = { ecran = EcranAdmin.Dashboard(e.token) },
            onAllerRepertoire = { ecran = EcranAdmin.Repertoire(e.token) },
            onAllerAssistant = { ecran = EcranAdmin.Assistant(e.token) },
            onDeconnexion = { ecran = EcranAdmin.Login }
        )

        is EcranAdmin.Assistant -> EcranAssistant(
            onAllerDashboard = { ecran = EcranAdmin.Dashboard(e.token) },
            onAllerRepertoire = { ecran = EcranAdmin.Repertoire(e.token) },
            onAllerParametres = { ecran = EcranAdmin.Parametres(e.token) },
            onDeconnexion = { ecran = EcranAdmin.Login }
        )

        is EcranAdmin.ProjectionQr -> EcranProjectionQr(
            port = 8080,
            onRetour = { ecran = e.retourVers }
        )

        is EcranAdmin.CreationScrutin -> EcranCreationScrutin(
            token = e.token,
            onAnnuler = { ecran = EcranAdmin.Dashboard(e.token) },
            onCree = { scrutin -> ecran = EcranAdmin.DetailScrutin(e.token, scrutin.id) }
        )

        is EcranAdmin.DetailScrutin -> EcranDetailScrutin(
            token = e.token,
            scrutinId = e.scrutinId,
            onRetour = { ecran = EcranAdmin.Dashboard(e.token) },
            onAjouterListe = { ecran = EcranAdmin.CreationListe(e.token, e.scrutinId) },
            onModifierListe = { liste ->
                ecran = EcranAdmin.ModificationListe(e.token, e.scrutinId, liste)
            },
            onAfficherResultats = { ecran = EcranAdmin.Resultats(e.token, e.scrutinId) }
        )

        is EcranAdmin.Resultats -> EcranResultats(
            token = e.token,
            scrutinId = e.scrutinId,
            onRetour = { ecran = EcranAdmin.DetailScrutin(e.token, e.scrutinId) }
        )

        is EcranAdmin.CreationListe -> EcranCreationListe(
            token = e.token,
            scrutinId = e.scrutinId,
            listeExistante = null,
            onAnnuler = { ecran = EcranAdmin.DetailScrutin(e.token, e.scrutinId) },
            onCree = { _ -> ecran = EcranAdmin.DetailScrutin(e.token, e.scrutinId) }
        )

        is EcranAdmin.ModificationListe -> EcranCreationListe(
            token = e.token,
            scrutinId = e.scrutinId,
            listeExistante = e.liste,
            onAnnuler = { ecran = EcranAdmin.DetailScrutin(e.token, e.scrutinId) },
            onCree = { _ -> ecran = EcranAdmin.DetailScrutin(e.token, e.scrutinId) }
        )
    }
}
