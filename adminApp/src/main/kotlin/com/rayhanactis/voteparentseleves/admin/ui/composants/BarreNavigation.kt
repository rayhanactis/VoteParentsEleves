package com.rayhanactis.voteparentseleves.admin.ui.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

enum class OngletAdmin { DASHBOARD, REPERTOIRE, PARAMETRES }

/**
 * Largeur en dessous de laquelle la barre passe en disposition « compacte » :
 * titre sur une ligne, puis onglets et actions répartis sur autant de lignes que
 * nécessaire (FlowRow). Au-dessus, tout tient sur une seule ligne.
 */
private val SEUIL_COMPACT = 1080.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarreNavigation(
    actif: OngletAdmin,
    onAllerDashboard: () -> Unit,
    onAllerRepertoire: () -> Unit,
    onAllerParametres: () -> Unit,
    onActualiser: (() -> Unit)? = null,
    onDeconnexion: (() -> Unit)? = null,
    onProjeterQr: (() -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp)
            .background(Color.White)
    ) {
        val compact = maxWidth < SEUIL_COMPACT

        if (compact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Titre()
                Spacer(Modifier.height(14.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Onglets(actif, onAllerDashboard, onAllerRepertoire, onAllerParametres)
                    Actions(onProjeterQr, onActualiser, onDeconnexion)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.padding(end = 32.dp)) { Titre() }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Onglets(actif, onAllerDashboard, onAllerRepertoire, onAllerParametres)
                }
                Spacer(Modifier.weight(1f))
                Actions(onProjeterQr, onActualiser, onDeconnexion)
            }
        }
    }
}

@Composable
private fun Titre() {
    Column {
        Text(
            text = "VoteParentsEleves",
            style = MaterialTheme.typography.titleLarge,
            color = Couleurs.NoirEncre,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = "Administration · serveur local 8080",
            style = MaterialTheme.typography.labelMedium,
            color = Couleurs.GrisDoux,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun Onglets(
    actif: OngletAdmin,
    onAllerDashboard: () -> Unit,
    onAllerRepertoire: () -> Unit,
    onAllerParametres: () -> Unit
) {
    Onglet("Scrutins", actif == OngletAdmin.DASHBOARD, onAllerDashboard)
    Onglet("Parents d'élèves", actif == OngletAdmin.REPERTOIRE, onAllerRepertoire)
    Onglet("Paramètres", actif == OngletAdmin.PARAMETRES, onAllerParametres)
}

@Composable
private fun Actions(
    onProjeterQr: (() -> Unit)?,
    onActualiser: (() -> Unit)?,
    onDeconnexion: (() -> Unit)?
) {
    if (onProjeterQr != null) BoutonLien("Projeter le QR", Couleurs.RosePop, onProjeterQr)
    if (onActualiser != null) BoutonLien("Actualiser", Couleurs.BleuKlein, onActualiser)
    if (onDeconnexion != null) BoutonLien("Se déconnecter", Couleurs.GrisDoux, onDeconnexion)
}

@Composable
private fun Onglet(texte: String, actif: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    val fond = if (actif) Couleurs.BleuKlein else Couleurs.NoirEncre.copy(alpha = 0.04f)
    val couleurTexte = if (actif) Color.White else Couleurs.NoirEncre
    Box(
        modifier = Modifier
            .clip(shape)
            .background(fond, shape)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.labelLarge,
            color = couleurTexte,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun BoutonLien(texte: String, couleur: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.labelLarge,
            color = couleur,
            maxLines = 1,
            softWrap = false
        )
    }
}
