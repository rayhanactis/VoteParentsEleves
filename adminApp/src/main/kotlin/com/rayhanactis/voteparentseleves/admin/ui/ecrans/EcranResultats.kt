package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.fichiers.choisirDestinationFichier
import com.rayhanactis.voteparentseleves.admin.pdf.genererPdfProcesVerbal
import com.rayhanactis.voteparentseleves.admin.ui.composants.ColonneDefilante
import com.rayhanactis.voteparentseleves.admin.viewmodel.DetailScrutinViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatDetail
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.ParticipationScrutin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun EcranResultats(
    token: String,
    scrutinId: String,
    onRetour: () -> Unit
) {
    val api = LocalApiClient.current
    val vm: DetailScrutinViewModel = viewModel { DetailScrutinViewModel(api, token) }
    val coroutineScope = rememberCoroutineScope()
    var messageExport by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scrutinId) {
        vm.charger(scrutinId)
        vm.rafraichirParticipation(scrutinId)
    }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreRetour(onRetour = onRetour)
            when (val etat = vm.etat) {
                EtatDetail.Chargement -> Centre("Chargement des résultats…")
                is EtatDetail.Erreur -> Centre(etat.message)
                is EtatDetail.Pret -> {
                    val resultats = etat.resultats
                    if (resultats == null) {
                        Centre("Les résultats ne sont pas encore disponibles.")
                    } else {
                        ContenuResultats(
                            scrutin = etat.scrutin,
                            listes = etat.listes,
                            resultats = resultats,
                            participation = vm.participation,
                            onExporter = {
                                coroutineScope.launch {
                                    val nom = etat.scrutin.nom.ifBlank { etat.scrutin.id }
                                    val fichier = withContext(Dispatchers.IO) {
                                        val destination = choisirDestinationFichier(
                                            titre = "Exporter les résultats",
                                            nomParDefaut = "resultats-$scrutinId.pdf"
                                        ) ?: return@withContext null
                                        genererPdfProcesVerbal(resultats, nom, destination)
                                        destination
                                    }
                                    messageExport = fichier?.let { "Résultats exportés : ${it.name}" }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    messageExport?.let { message ->
        AlertDialog(
            onDismissRequest = { messageExport = null },
            title = { Text("Export PDF") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { messageExport = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun ContenuResultats(
    scrutin: Scrutin,
    listes: List<ListeCandidate>,
    resultats: ResultatScrutin,
    participation: ParticipationScrutin?,
    onExporter: () -> Unit
) {
    val nomListe: (String) -> String = { id -> listes.firstOrNull { it.id == id }?.nom ?: id }
    val couleurListe: (String) -> Color = { id ->
        val idx = listes.indexOfFirst { it.id == id }.let { if (it < 0) 0 else it }
        Couleurs.PaletteListes[idx % Couleurs.PaletteListes.size]
    }

    val classement = resultats.resultatsParListe.entries.sortedByDescending { it.value }
    val totalExprimes = resultats.resultatsParListe.values.sum()
    val maxVoix = classement.firstOrNull()?.value ?: 0
    val nbVotants = participation?.nbVotants ?: 0
    val blancs = max(0, nbVotants - totalExprimes)

    ColonneDefilante(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Résultats officiels",
                    style = MaterialTheme.typography.labelLarge,
                    color = Couleurs.BleuKlein
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = scrutin.nom.ifBlank { scrutin.id },
                    style = MaterialTheme.typography.headlineLarge,
                    color = Couleurs.NoirEncre,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${scrutin.nbSieges} sièges à pourvoir · scrutin de liste (méthode de Hare)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CarteStat(
                    valeur = (participation?.totalElecteurs ?: 0).toString(),
                    libelle = "Inscrits",
                    couleur = Couleurs.BleuKlein,
                    modifier = Modifier.weight(1f)
                )
                CarteStat(
                    valeur = nbVotants.toString(),
                    libelle = "Votants",
                    couleur = Couleurs.VertMenthe,
                    modifier = Modifier.weight(1f)
                )
                CarteStat(
                    valeur = "${((participation?.tauxParticipation ?: 0f) * 100).roundToInt()} %",
                    libelle = "Participation",
                    couleur = Couleurs.OrangeCh,
                    modifier = Modifier.weight(1f)
                )
                CarteStat(
                    valeur = blancs.toString(),
                    libelle = "Votes blancs",
                    couleur = Couleurs.GrisDoux,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Répartition des sièges",
                style = MaterialTheme.typography.headlineMedium,
                color = Couleurs.NoirEncre
            )

            classement.forEachIndexed { rang, (listeId, voix) ->
                LigneResultat(
                    rang = rang + 1,
                    nom = nomListe(listeId),
                    voix = voix,
                    sieges = resultats.siegesAttribues[listeId] ?: 0,
                    fraction = if (maxVoix <= 0) 0f else voix.toFloat() / maxVoix,
                    partExprimes = if (totalExprimes <= 0) 0f else voix.toFloat() / totalExprimes,
                    couleur = couleurListe(listeId)
                )
            }

            BoutonClay(
                texte = "EXPORTER LES RÉSULTATS EN PDF",
                onClick = onExporter,
                modifier = Modifier.fillMaxWidth(),
                couleur = Couleurs.VertMenthe
            )
            Text(
                text = "Le PDF reprend le procès-verbal officiel : prêt à imprimer et afficher.",
                style = MaterialTheme.typography.bodySmall,
                color = Couleurs.GrisDoux,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CarteStat(valeur: String, libelle: String, couleur: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, couleur.copy(alpha = 0.25f), shape)
            .padding(vertical = 18.dp, horizontal = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = valeur,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = couleur,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = libelle,
                style = MaterialTheme.typography.bodySmall,
                color = Couleurs.GrisDoux,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LigneResultat(
    rang: Int,
    nom: String,
    voix: Int,
    sieges: Int,
    fraction: Float,
    partExprimes: Float,
    couleur: Color
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, couleur.copy(alpha = 0.3f), shape)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(couleur, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rang.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.size(14.dp))
                Text(
                    text = nom,
                    style = MaterialTheme.typography.titleLarge,
                    color = Couleurs.NoirEncre,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(couleur, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (sieges > 1) "$sieges sièges" else "$sieges siège",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(couleur.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(couleur)
                )
            }

            Text(
                text = "$voix voix · ${(partExprimes * 100).roundToInt()} % des suffrages exprimés",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )
        }
    }
}

@Composable
private fun BarreRetour(onRetour: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onRetour() }
                .background(Couleurs.BleuKlein.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "← Retour au scrutin",
                style = MaterialTheme.typography.labelLarge,
                color = Couleurs.BleuKlein
            )
        }
    }
}

@Composable
private fun Centre(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Couleurs.BleuKlein)
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Couleurs.NoirEncre,
                textAlign = TextAlign.Center
            )
        }
    }
}
