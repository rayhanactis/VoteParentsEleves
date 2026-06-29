package com.rayhanactis.voteparentseleves.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.BoutonLireVoixHaute
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranRecu(
    bulletinId: String,
    nomListe: String?,
    onRetour: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 480.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(elevation = 20.dp, shape = CircleShape, ambientColor = Couleurs.VertMenthe)
                            .background(Couleurs.VertMenthe, CircleShape)
                            .border(4.dp, Couleurs.NoirEncre, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.displayLarge,
                            color = Couleurs.BlancCasse
                        )
                    }

                    Text(
                        text = "Merci !",
                        style = MaterialTheme.typography.displayLarge,
                        color = Couleurs.NoirEncre
                    )
                    Text(
                        text = "Votre vote a bien été enregistré.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux,
                        textAlign = TextAlign.Center
                    )

                    if (nomListe != null) {
                        Text(
                            text = "Liste choisie : $nomListe",
                            style = MaterialTheme.typography.titleMedium,
                            color = Couleurs.NoirEncre
                        )
                    } else {
                        Text(
                            text = "Vote blanc enregistré.",
                            style = MaterialTheme.typography.titleMedium,
                            color = Couleurs.NoirEncre
                        )
                    }

                    Text(
                        text = "Reçu n° $bulletinId",
                        style = MaterialTheme.typography.labelMedium,
                        color = Couleurs.GrisDoux
                    )

                    BoutonLireVoixHaute(
                        texte = "Merci. Votre vote a bien été enregistré. " +
                            (nomListe?.let { "Liste choisie : $it." } ?: "Vote blanc enregistré.") +
                            " Reçu numéro $bulletinId."
                    )

                    Spacer(Modifier.height(8.dp))

                    BoutonClay(
                        texte = "Terminer",
                        onClick = onRetour,
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.BleuKlein
                    )
                }
            }
        }
    }
}
