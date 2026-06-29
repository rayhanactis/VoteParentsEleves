package com.rayhanactis.voteparentseleves.ui.ecrans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.ui.composants.CarteGlass
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranChoixScrutin(
    scrutins: List<Scrutin>,
    onChoisir: (Scrutin) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quel scrutin ?",
                    style = MaterialTheme.typography.displayMedium,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = "Plusieurs scrutins sont ouverts sur ce serveur. Choisissez le vôtre.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Couleurs.GrisDoux
                )
                scrutins.forEach { scrutin ->
                    CarteGlass(modifier = Modifier.fillMaxWidth().clickable { onChoisir(scrutin) }) {
                        Text(
                            text = scrutin.nom.ifBlank { scrutin.id },
                            style = MaterialTheme.typography.titleLarge,
                            color = Couleurs.NoirEncre,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
