package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.ui.composants.SelecteurDateHeure
import com.rayhanactis.voteparentseleves.admin.viewmodel.CreationScrutinViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatCreationScrutin
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import java.time.LocalDateTime

@Composable
fun EcranCreationScrutin(
    token: String,
    onAnnuler: () -> Unit,
    onCree: (Scrutin) -> Unit
) {
    val api = LocalApiClient.current
    val vm: CreationScrutinViewModel = viewModel { CreationScrutinViewModel(api, token) }

    val maintenant = remember {
        LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    }
    var nom by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf(maintenant.withHour(8)) }
    var dateFin by remember { mutableStateOf(maintenant.plusDays(7).withHour(18)) }
    var nbSieges by remember { mutableStateOf("3") }

    val etat = vm.etat
    val envoi = etat is EtatCreationScrutin.Envoi

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Couleurs.FondCreme)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 720.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nouveau scrutin",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Couleurs.NoirEncre
                    )
                    Text(
                        text = "Renseignez les paramètres du scrutin. Vous pourrez ensuite ajouter les listes candidates et l'ouvrir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Couleurs.GrisDoux
                    )

                    ChampTexte(
                        valeur = nom,
                        onChange = { nom = it; vm.resetErreur() },
                        label = "Nom du scrutin",
                        placeholder = "ex : Élections représentants 2026"
                    )

                    SelecteurDateHeure(
                        label = "Date et heure d'ouverture",
                        valeur = dateDebut,
                        onChange = { dateDebut = it; vm.resetErreur() }
                    )

                    SelecteurDateHeure(
                        label = "Date et heure de fermeture",
                        valeur = dateFin,
                        onChange = { dateFin = it; vm.resetErreur() }
                    )

                    ChampTexte(
                        valeur = nbSieges,
                        onChange = { nbSieges = it.filter { c -> c.isDigit() }; vm.resetErreur() },
                        label = "Nombre de sièges à pourvoir",
                        placeholder = "ex : 5",
                        typeClavier = KeyboardType.Number
                    )

                    if (etat is EtatCreationScrutin.Erreur) MessageErreur(etat.message)

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BoutonClay(
                            texte = "Annuler",
                            onClick = onAnnuler,
                            modifier = Modifier.weight(1f),
                            couleur = Couleurs.GrisDoux,
                            enabled = !envoi
                        )
                        BoutonClay(
                            texte = if (envoi) "CRÉATION…" else "CRÉER LE SCRUTIN",
                            onClick = {
                                val sieges = nbSieges.toIntOrNull() ?: 0
                                vm.creer(nom, dateDebut, dateFin, sieges, onCree)
                            },
                            modifier = Modifier.weight(1.4f),
                            couleur = Couleurs.BleuKlein,
                            enabled = !envoi
                        )
                    }
                }
            }
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
