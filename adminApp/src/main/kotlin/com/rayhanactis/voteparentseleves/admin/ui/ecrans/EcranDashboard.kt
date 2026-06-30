package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import com.rayhanactis.voteparentseleves.admin.ui.composants.LazyColonneDefilante
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.DashboardViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatDashboard
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranDashboard(
    token: String,
    onDeconnexion: () -> Unit,
    onNouveauScrutin: () -> Unit,
    onSelectionnerScrutin: (Scrutin) -> Unit,
    onAllerRepertoire: () -> Unit,
    onAllerParametres: () -> Unit,
    onProjeterQr: () -> Unit
) {
    val api = LocalApiClient.current
    val vm: DashboardViewModel = viewModel { DashboardViewModel(api) }

    LaunchedEffect(Unit) { vm.charger() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Couleurs.FondCreme)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            com.rayhanactis.voteparentseleves.admin.ui.composants.BarreNavigation(
                actif = com.rayhanactis.voteparentseleves.admin.ui.composants.OngletAdmin.DASHBOARD,
                onAllerDashboard = { vm.charger() },
                onAllerRepertoire = onAllerRepertoire,
                onAllerParametres = onAllerParametres,
                onActualiser = { vm.charger() },
                onDeconnexion = onDeconnexion,
                onProjeterQr = onProjeterQr
            )

            when (val etat = vm.etat) {
                EtatDashboard.Chargement -> Centre("Chargement des scrutins…", spinner = true)
                is EtatDashboard.Erreur -> CentreErreur(
                    message = etat.message,
                    onReessayer = { vm.charger() }
                )
                is EtatDashboard.Pret -> ContenuListeScrutins(
                    scrutins = etat.scrutins,
                    onNouveauScrutin = onNouveauScrutin,
                    onSelection = onSelectionnerScrutin
                )
            }
        }
    }
}

@Composable
private fun ContenuListeScrutins(
    scrutins: List<Scrutin>,
    onNouveauScrutin: () -> Unit,
    onSelection: (Scrutin) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 900.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mes scrutins",
                style = MaterialTheme.typography.headlineLarge,
                color = Couleurs.NoirEncre,
                modifier = Modifier.weight(1f)
            )
            BoutonClay(
                texte = "+ Nouveau scrutin",
                onClick = onNouveauScrutin,
                couleur = Couleurs.BleuKlein
            )
        }

        Spacer(Modifier.height(20.dp))

        if (scrutins.isEmpty()) {
            EtatVide(onNouveauScrutin)
        } else {
            LazyColonneDefilante(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 900.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(scrutins) { scrutin ->
                    CarteScrutin(scrutin = scrutin, onClick = { onSelection(scrutin) })
                }
            }
        }
    }
}

@Composable
private fun CarteScrutin(scrutin: Scrutin, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val (couleurStatut, libelleStatut) = statutDisplay(scrutin.statut)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, Color.Black.copy(alpha = 0.06f), shape)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(couleurStatut, CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
            )
            Spacer(Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scrutin.nom.ifBlank { scrutin.id },
                    style = MaterialTheme.typography.titleLarge,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = "École : ${scrutin.ecoleId} · ${scrutin.nbSieges} sièges",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
                if (scrutin.statut == StatutScrutin.Programme) {
                    Text(
                        text = "Ouverture programmée pour le ${formatDateHeure(scrutin.dateDebut)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Couleurs.OrangeCh
                    )
                }
            }
            BadgeStatut(couleurStatut, libelleStatut)
        }
    }
}

@Composable
private fun BadgeStatut(couleur: Color, libelle: String) {
    Box(
        modifier = Modifier
            .background(couleur, RoundedCornerShape(percent = 50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = libelle.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

private fun statutDisplay(statut: StatutScrutin): Pair<Color, String> = when (statut) {
    StatutScrutin.Configure -> Couleurs.GrisDoux to "En configuration"
    StatutScrutin.Programme -> Couleurs.OrangeCh to "Programmé"
    StatutScrutin.Ouvert -> Couleurs.VertMenthe to "Ouvert au vote"
    StatutScrutin.Ferme -> Couleurs.JaunePop to "Fermé"
    StatutScrutin.Depouille -> Couleurs.BleuKlein to "Dépouillé"
}

private val formatterDateHeure =
    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")
        .withZone(java.time.ZoneId.systemDefault())

private fun formatDateHeure(epochMillis: Long): String =
    if (epochMillis == Long.MAX_VALUE) "∞"
    else formatterDateHeure.format(java.time.Instant.ofEpochMilli(epochMillis))

@Composable
private fun EtatVide(onNouveauScrutin: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp)
    ) {
        Text(
            text = "Aucun scrutin pour l'instant.",
            style = MaterialTheme.typography.titleLarge,
            color = Couleurs.NoirEncre,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Créez votre premier scrutin pour commencer.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        BoutonClay(
            texte = "+ Nouveau scrutin",
            onClick = onNouveauScrutin,
            couleur = Couleurs.BleuKlein
        )
    }
}

@Composable
private fun Centre(message: String, spinner: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (spinner) {
                CircularProgressIndicator(color = Couleurs.BleuKlein)
                Spacer(Modifier.height(16.dp))
            }
            Text(message, style = MaterialTheme.typography.bodyLarge, color = Couleurs.NoirEncre)
        }
    }
}

@Composable
private fun CentreErreur(message: String, onReessayer: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Couleurs.RougePompidou,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            BoutonClay(
                texte = "Réessayer",
                onClick = onReessayer,
                modifier = Modifier.fillMaxWidth(),
                couleur = Couleurs.BleuKlein
            )
        }
    }
}

