package com.rayhanactis.voteparentseleves.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.BoutonLireVoixHaute
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import com.rayhanactis.voteparentseleves.viewmodel.EtatVote
import com.rayhanactis.voteparentseleves.viewmodel.VoteViewModel
import com.rayhanactis.voteparentseleves.vote.RecuVote

@Composable
fun EcranConfirmation(
    token: String,
    scrutinId: String,
    choix: ChoixVote,
    onModifier: () -> Unit,
    onVoteEnregistre: (RecuVote) -> Unit
) {
    val api = LocalApiClient.current
    val vm: VoteViewModel = viewModel { VoteViewModel(api) }
    val etat = vm.etat
    val envoi = etat is EtatVote.Envoi

    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 520.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(
                        text = "Je vérifie mon choix",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Couleurs.NoirEncre
                    )
                    Text(
                        text = "Une fois confirmé, votre vote sera enregistré et ne pourra plus être modifié.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux
                    )

                    RecapChoix(choix = choix)

                    BoutonLireVoixHaute(
                        texte = "Je vérifie mon choix. Vous avez choisi : ${choix.libelle()}. " +
                            "Une fois confirmé, votre vote sera enregistré et ne pourra plus être modifié."
                    )

                    if (etat is EtatVote.Erreur) {
                        MessageErreur(etat.message)
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BoutonClay(
                            texte = "Modifier",
                            onClick = onModifier,
                            modifier = Modifier.weight(1f),
                            couleur = Couleurs.JaunePop,
                            couleurContenu = Couleurs.NoirEncre,
                            enabled = !envoi,
                            style = MaterialTheme.typography.labelMedium
                        )
                        BoutonClay(
                            texte = if (envoi) "ENVOI…" else "Je confirme  ✓",
                            onClick = {
                                vm.deposer(token, scrutinId, choix, onVoteEnregistre)
                            },
                            modifier = Modifier.weight(1.4f),
                            couleur = Couleurs.VertMenthe,
                            enabled = !envoi,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecapChoix(choix: ChoixVote) {
    val shape = RoundedCornerShape(24.dp)
    val couleur = when (choix) {
        is ChoixVote.PourListe -> Couleurs.BleuKlein
        ChoixVote.Blanc -> Couleurs.NoirEncre
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = shape, ambientColor = couleur.copy(alpha = 0.4f))
            .background(couleur, shape)
            .border(2.dp, Color.White.copy(alpha = 0.6f), shape)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            Text(
                text = "Mon vote",
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.BlancCasse.copy(alpha = 0.85f)
            )
            Text(
                text = choix.libelle(),
                style = MaterialTheme.typography.displayMedium,
                color = Couleurs.BlancCasse
            )
        }
    }
}

@Composable
private fun MessageErreur(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Couleurs.RougePompidou.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Couleurs.RougePompidou
        )
    }
}
