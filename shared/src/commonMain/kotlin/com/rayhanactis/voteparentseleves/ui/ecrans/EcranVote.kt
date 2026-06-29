package com.rayhanactis.voteparentseleves.ui.ecrans

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.BoutonLireVoixHaute
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranVote(
    listes: List<ListeCandidate>,
    onValider: (ChoixVote) -> Unit,
    onRevoirListes: () -> Unit
) {
    var choix by remember { mutableStateOf<ChoixVote?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnteteVote()

            Spacer(Modifier.height(8.dp))
            BoutonLireVoixHaute(texte = construireTexteVocalVote(listes))
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(listes) { liste ->
                    val index = listes.indexOf(liste)
                    val couleur = Couleurs.PaletteListes[index % Couleurs.PaletteListes.size]
                    val choisi = (choix as? ChoixVote.PourListe)?.listeId == liste.id
                    CarteListe(
                        liste = liste,
                        couleur = couleur,
                        choisi = choisi,
                        onClick = { choix = ChoixVote.PourListe(liste.id, liste.nom) }
                    )
                }
                item {
                    CarteVoteBlanc(
                        choisi = choix is ChoixVote.Blanc,
                        onClick = { choix = ChoixVote.Blanc }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            BoutonClay(
                texte = if (choix == null) "CHOISIR UNE LISTE" else "VALIDER MON CHOIX  →",
                onClick = { choix?.let { onValider(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                couleur = if (choix == null) Couleurs.GrisDoux else Couleurs.BleuKlein,
                enabled = choix != null
            )

            Spacer(Modifier.height(10.dp))

            LienRevoirListes(onClick = onRevoirListes)
        }
    }
}

private fun construireTexteVocalVote(listes: List<ListeCandidate>): String {
    val noms = listes.joinToString(", ") { it.nom }
    return "Je choisis ma liste. Touchez une liste pour la sélectionner, ou choisissez le vote blanc. " +
        "Listes disponibles : $noms."
}

@Composable
private fun LienRevoirListes(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "← Revoir le détail des listes",
            style = MaterialTheme.typography.labelMedium,
            color = Couleurs.BleuKlein
        )
    }
}

@Composable
private fun EnteteVote() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Je choisis ma liste",
            style = MaterialTheme.typography.displayMedium,
            color = Couleurs.NoirEncre
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Touchez une liste pour la sélectionner.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux
        )
    }
}

@Composable
private fun CarteListe(
    liste: ListeCandidate,
    couleur: Color,
    choisi: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val gradient = Brush.linearGradient(
        listOf(
            couleur.brouille(Color.White, 0.10f),
            couleur,
            couleur.brouille(Color.Black, 0.08f)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (choisi) 22.dp else 12.dp,
                shape = shape,
                ambientColor = couleur.copy(alpha = 0.5f),
                spotColor = couleur.copy(alpha = 0.35f)
            )
            .background(gradient, shape)
            .border(
                width = if (choisi) 4.dp else 2.dp,
                color = if (choisi) Couleurs.NoirEncre else Color.White.copy(alpha = 0.6f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(3.dp, Couleurs.NoirEncre.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (liste.nom.firstOrNull() ?: '?').uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Couleurs.NoirEncre
                )
            }
            Spacer(Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = liste.nom,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Couleurs.BlancCasse
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${liste.candidats.size} candidat·es",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.BlancCasse.copy(alpha = 0.9f)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (choisi) Couleurs.NoirEncre else Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (choisi) {
                    Text("✓", style = MaterialTheme.typography.headlineMedium, color = Couleurs.BlancCasse)
                }
            }
        }
    }
}

@Composable
private fun CarteVoteBlanc(
    choisi: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (choisi) 18.dp else 8.dp, shape = shape)
            .background(Color.White, shape)
            .border(
                width = if (choisi) 4.dp else 2.dp,
                color = if (choisi) Couleurs.NoirEncre else Couleurs.NoirEncre.copy(alpha = 0.15f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Couleurs.FondCreme, CircleShape)
                    .border(3.dp, Couleurs.NoirEncre.copy(alpha = 0.4f), CircleShape)
            )
            Spacer(Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vote blanc",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = "Je ne choisis aucune liste.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (choisi) Couleurs.NoirEncre else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(2.dp, Couleurs.NoirEncre.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (choisi) {
                    Text("✓", style = MaterialTheme.typography.headlineMedium, color = Couleurs.BlancCasse)
                }
            }
        }
    }
}

private fun Color.brouille(autre: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = red + (autre.red - red) * r,
        green = green + (autre.green - green) * r,
        blue = blue + (autre.blue - blue) * r,
        alpha = alpha
    )
}
