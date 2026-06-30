package com.rayhanactis.voteparentseleves.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Candidat
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.BoutonLireVoixHaute
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.mock.MockData
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import com.rayhanactis.voteparentseleves.viewmodel.EtatListes
import com.rayhanactis.voteparentseleves.viewmodel.ListesViewModel

@Composable
fun EcranListes(
    scrutinId: String,
    onContinuerVersVote: (List<ListeCandidate>) -> Unit
) {
    val api = LocalApiClient.current
    val vm: ListesViewModel = viewModel { ListesViewModel(api) }

    LaunchedEffect(scrutinId) { vm.charger(scrutinId) }

    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()

        when (val etat = vm.etat) {
            EtatListes.Chargement -> CentreSpinner("Chargement des listes…")
            is EtatListes.Erreur -> CentreErreur(
                message = etat.message,
                onReessayer = { vm.charger(scrutinId) }
            )
            is EtatListes.Pretes -> ContenuListes(
                listes = etat.listes,
                onContinuerVersVote = { onContinuerVersVote(etat.listes) }
            )
        }
    }
}

@Composable
private fun ContenuListes(
    listes: List<ListeCandidate>,
    onContinuerVersVote: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnteteListes()
        Spacer(Modifier.height(8.dp))
        BoutonLireVoixHaute(texte = construireTexteVocalListes(listes))
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(listes) { liste ->
                val index = listes.indexOf(liste)
                val couleur = Couleurs.PaletteListes[index % Couleurs.PaletteListes.size]
                val description = liste.description.ifBlank {
                    MockData.descriptions[liste.id].orEmpty()
                }
                CartePresentation(
                    liste = liste,
                    couleur = couleur,
                    slogan = liste.slogan,
                    description = description
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        BoutonClay(
            texte = "J'AI COMPRIS, JE VOTE  →",
            onClick = onContinuerVersVote,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            couleur = Couleurs.BleuKlein,
            enabled = listes.isNotEmpty()
        )
    }
}

private fun construireTexteVocalListes(listes: List<ListeCandidate>): String {
    if (listes.isEmpty()) return "Les listes en présence. Aucune liste pour l'instant."
    val detail = listes.joinToString(" ") { liste ->
        val candidats = liste.candidats.joinToString(", ") { "${it.prenom} ${it.nom}" }
        "Liste ${liste.nom}. ${liste.slogan}. Candidats : $candidats."
    }
    return "Les listes en présence. $detail"
}

@Composable
private fun EnteteListes() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Les listes en présence",
            style = MaterialTheme.typography.displayMedium,
            color = Couleurs.NoirEncre
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Prenez le temps de les découvrir avant de voter.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux
        )
    }
}

@Composable
private fun CartePresentation(
    liste: ListeCandidate,
    couleur: Color,
    slogan: String,
    description: String
) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(elevation = 14.dp, shape = shape, ambientColor = couleur.copy(alpha = 0.4f))
            .clip(shape)
            .background(Color.White)
            .border(2.dp, couleur.copy(alpha = 0.25f), shape)
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight()
                .background(couleur)
        )

        Column(modifier = Modifier.padding(start = 20.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(6.dp, CircleShape)
                        .background(couleur, CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (liste.nom.firstOrNull() ?: '?').uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Couleurs.BlancCasse
                    )
                }
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = liste.nom,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Couleurs.NoirEncre
                    )
                    Text(
                        text = "${liste.candidats.size} candidat·es",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Couleurs.GrisDoux
                    )
                }
            }

            if (slogan.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = slogan,
                    style = MaterialTheme.typography.titleMedium,
                    color = couleur
                )
            }

            if (description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "« $description »",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = Couleurs.NoirEncre
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "Candidat·es :",
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.GrisDoux
            )
            Spacer(Modifier.height(8.dp))
            liste.candidats.forEach { candidat ->
                LigneCandidat(candidat = candidat, couleur = couleur)
            }
        }
    }
}

@Composable
private fun LigneCandidat(candidat: Candidat, couleur: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(couleur.copy(alpha = 0.18f), CircleShape)
                .border(1.5.dp, couleur, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${candidat.prenom.firstOrNull() ?: ""}${candidat.nom.firstOrNull() ?: ""}".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.NoirEncre
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = "${candidat.prenom} ${candidat.nom}",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.NoirEncre
        )
    }
}

@Composable
private fun CentreSpinner(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Couleurs.BleuKlein)
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Couleurs.NoirEncre
            )
        }
    }
}

@Composable
private fun CentreErreur(message: String, onReessayer: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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
