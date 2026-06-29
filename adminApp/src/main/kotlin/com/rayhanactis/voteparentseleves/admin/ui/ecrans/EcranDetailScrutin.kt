package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.fichiers.choisirDestinationFichier
import com.rayhanactis.voteparentseleves.admin.pdf.genererPdfProcesVerbal
import com.rayhanactis.voteparentseleves.admin.viewmodel.DetailScrutinViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatDetail
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.ParticipationScrutin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun EcranDetailScrutin(
    token: String,
    scrutinId: String,
    onRetour: () -> Unit,
    onAjouterListe: () -> Unit,
    onModifierListe: (ListeCandidate) -> Unit
) {
    val api = LocalApiClient.current
    val vm: DetailScrutinViewModel = viewModel { DetailScrutinViewModel(api, token) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(scrutinId) { vm.charger(scrutinId) }

    var dialogRenommer by remember { mutableStateOf(false) }
    var dialogSuppressionScrutin by remember { mutableStateOf(false) }
    var listeASupprimer by remember { mutableStateOf<ListeCandidate?>(null) }
    var messageExportPv by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreSuperieureDetail(onRetour = onRetour, onActualiser = { vm.charger(scrutinId) })
            when (val etat = vm.etat) {
                EtatDetail.Chargement -> Centre("Chargement du scrutin…", spinner = true)
                is EtatDetail.Erreur -> CentreErreur(etat.message) { vm.charger(scrutinId) }
                is EtatDetail.Pret -> {
                    // Suivi de participation en temps réel : on interroge le
                    // serveur toutes les 5 s tant que le scrutin est ouvert.
                    if (etat.scrutin.statut == StatutScrutin.Ouvert) {
                        LaunchedEffect(scrutinId) {
                            while (true) {
                                vm.rafraichirParticipation(scrutinId)
                                delay(5_000)
                            }
                        }
                    }
                    Contenu(
                    scrutin = etat.scrutin,
                    listes = etat.listes,
                    resultats = etat.resultats,
                    participation = vm.participation,
                    actionEnCours = vm.actionEnCours,
                    onOuvrir = { vm.ouvrir(scrutinId) },
                    onFermer = { vm.fermer(scrutinId) },
                    onDepouiller = { vm.depouiller(scrutinId) },
                    onAjouterListe = onAjouterListe,
                    onRenommer = { dialogRenommer = true },
                    onSupprimerScrutin = { dialogSuppressionScrutin = true },
                    onModifierListe = onModifierListe,
                    onSupprimerListe = { listeASupprimer = it },
                    onExporterPv = { resultats ->
                        coroutineScope.launch {
                            val nomScrutin = etat.scrutin.nom.ifBlank { etat.scrutin.id }
                            val resultat = withContext(Dispatchers.IO) {
                                val destination = choisirDestinationFichier(
                                    titre = "Exporter le procès-verbal",
                                    nomParDefaut = "pv-$scrutinId.pdf"
                                ) ?: return@withContext null
                                genererPdfProcesVerbal(resultats, nomScrutin, destination)
                                destination
                            }
                            messageExportPv = if (resultat != null) "PV exporté : ${resultat.name}" else null
                        }
                    }
                    )
                }
            }
        }
    }

    messageExportPv?.let { message ->
        AlertDialog(
            onDismissRequest = { messageExportPv = null },
            title = { Text("Information") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { messageExportPv = null }) { Text("OK") } }
        )
    }

    val etatActuel = vm.etat
    if (dialogRenommer && etatActuel is EtatDetail.Pret) {
        DialogueRenommer(
            nomActuel = etatActuel.scrutin.nom.ifBlank { etatActuel.scrutin.id },
            onAnnuler = { dialogRenommer = false },
            onConfirmer = { nouveau ->
                dialogRenommer = false
                vm.renommer(scrutinId, nouveau)
            }
        )
    }
    if (dialogSuppressionScrutin && etatActuel is EtatDetail.Pret) {
        DialogueConfirmationSuppression(
            titre = "Êtes-vous sûr·e ?",
            corps = "Cette action est irréversible. Toutes les listes et candidats associés " +
                "seront également supprimés. La suppression sera refusée si au moins un " +
                "bulletin a déjà été déposé (vraie élection).",
            onAnnuler = { dialogSuppressionScrutin = false },
            onConfirmer = {
                dialogSuppressionScrutin = false
                vm.supprimerScrutin(scrutinId, onSupprime = onRetour)
            }
        )
    }
    listeASupprimer?.let { liste ->
        DialogueConfirmationSuppression(
            titre = "Supprimer la liste « ${liste.nom} » ?",
            corps = "Tous les candidats de cette liste seront également supprimés. Action irréversible.",
            onAnnuler = { listeASupprimer = null },
            onConfirmer = {
                listeASupprimer = null
                vm.supprimerListe(scrutinId, liste.id)
            }
        )
    }
}

@Composable
private fun BarreSuperieureDetail(onRetour: () -> Unit, onActualiser: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 2.dp)
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Lien(texte = "← Retour au dashboard", couleur = Couleurs.BleuKlein, onClick = onRetour)
        Box(modifier = Modifier.weight(1f))
        Lien(texte = "Actualiser", couleur = Couleurs.GrisDoux, onClick = onActualiser)
    }
}

@Composable
private fun Lien(texte: String, couleur: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = texte, style = MaterialTheme.typography.labelLarge, color = couleur)
    }
}

@Composable
private fun Contenu(
    scrutin: Scrutin,
    listes: List<ListeCandidate>,
    resultats: ResultatScrutin?,
    participation: ParticipationScrutin?,
    actionEnCours: Boolean,
    onOuvrir: () -> Unit,
    onFermer: () -> Unit,
    onDepouiller: () -> Unit,
    onAjouterListe: () -> Unit,
    onRenommer: () -> Unit,
    onSupprimerScrutin: () -> Unit,
    onModifierListe: (ListeCandidate) -> Unit,
    onSupprimerListe: (ListeCandidate) -> Unit,
    onExporterPv: (ResultatScrutin) -> Unit
) {
    val configurable = scrutin.statut == StatutScrutin.Configure
    // Toute la page scrolle d'un bloc : l'en-tête (infos, actions OUVRIR/FERMER/
    // DÉPOUILLER, résultats, identifiants) est un item, les listes en sont d'autres.
    // Évite que le contenu soit coupé quand la fenêtre est courte.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                EnteteScrutin(scrutin = scrutin, onRenommer = onRenommer)
                Spacer(Modifier.height(20.dp))
                ActionsScrutin(
                    statut = scrutin.statut,
                    actionEnCours = actionEnCours,
                    onOuvrir = onOuvrir,
                    onFermer = onFermer,
                    onDepouiller = onDepouiller
                )
                if (scrutin.statut == StatutScrutin.Ouvert) {
                    Spacer(Modifier.height(20.dp))
                    PanneauElectionEnCours(participation)
                }
                if (resultats != null) {
                    Spacer(Modifier.height(20.dp))
                    ResultatsCard(
                        resultats = resultats,
                        listes = listes,
                        onExporterPv = { onExporterPv(resultats) }
                    )
                }
                // Suppression : tentable à tout statut. Le serveur la refusera
                // si au moins un bulletin a été déposé (vraie élection).
                Spacer(Modifier.height(20.dp))
                BoutonClay(
                    texte = "Supprimer ce scrutin",
                    onClick = onSupprimerScrutin,
                    modifier = Modifier.fillMaxWidth(),
                    couleur = Couleurs.GrisDoux,
                    enabled = !actionEnCours
                )
                Spacer(Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Listes candidates (${listes.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Couleurs.NoirEncre,
                        modifier = Modifier.weight(1f)
                    )
                    if (configurable) {
                        BoutonClay(
                            texte = "+ Ajouter une liste",
                            onClick = onAjouterListe,
                            couleur = Couleurs.BleuKlein
                        )
                    }
                }
            }
        }

        if (listes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                    ListeVide(statut = scrutin.statut, onAjouterListe = onAjouterListe)
                }
            }
        } else {
            itemsIndexed(listes) { index, liste ->
                val couleur = Couleurs.PaletteListes[index % Couleurs.PaletteListes.size]
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                    CarteListe(
                        liste = liste,
                        couleur = couleur,
                        configurable = configurable,
                        onModifier = { onModifierListe(liste) },
                        onSupprimer = { onSupprimerListe(liste) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnteteScrutin(scrutin: Scrutin, onRenommer: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val (couleurStatut, libelleStatut) = statutDisplay(scrutin.statut)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, Color.Black.copy(alpha = 0.06f), shape)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(couleurStatut, CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .shadow(6.dp, CircleShape)
            )
            Spacer(Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = scrutin.nom.ifBlank { scrutin.id },
                        style = MaterialTheme.typography.titleLarge,
                        color = Couleurs.NoirEncre
                    )
                    Spacer(Modifier.size(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRenommer() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "renommer",
                            style = MaterialTheme.typography.labelMedium,
                            color = Couleurs.BleuKlein
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "École : ${scrutin.ecoleId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
                Text(
                    text = "Du ${formatDate(scrutin.dateDebut)} au ${formatDate(scrutin.dateFin)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
                Text(
                    text = "${scrutin.nbSieges} sièges à pourvoir",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
            BadgeStatut(couleurStatut, libelleStatut)
        }
    }
}

@Composable
private fun PanneauElectionEnCours(participation: ParticipationScrutin?) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = shape, ambientColor = Couleurs.VertMenthe.copy(alpha = 0.35f))
            .clip(shape)
    ) {
        // Backdrop coloré : ce sont ces formes que l'on devine à travers le verre.
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(Couleurs.VertMenthe.copy(alpha = 0.95f), radius = h * 0.85f, center = Offset(w * 0.10f, h * 0.12f))
            drawCircle(Couleurs.BleuKlein.copy(alpha = 0.85f), radius = h * 0.7f, center = Offset(w * 0.96f, h * 1.05f))
            rotate(degrees = 18f, pivot = Offset(w * 0.72f, h * 0.25f)) {
                drawRect(
                    color = Couleurs.JaunePop.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.60f, -h * 0.2f),
                    size = Size(h * 0.65f, h * 0.65f)
                )
            }
            drawCircle(Couleurs.RosePop.copy(alpha = 0.7f), radius = h * 0.16f, center = Offset(w * 0.48f, h * 0.95f))
        }
        // Couche "verre dépoli" semi-transparente : laisse transparaître les formes.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.60f), Color.White.copy(alpha = 0.36f))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.65f), shape)
        )
        // Contenu, avec une marge généreuse vis-à-vis des bords.
        Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Couleurs.VertMenthe, CircleShape)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "Élection en cours",
                    style = MaterialTheme.typography.titleLarge,
                    color = Couleurs.NoirEncre
                )
            }
            Spacer(Modifier.height(18.dp))

            if (participation == null) {
                Text(
                    text = "Mise à jour de la participation…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            } else {
                val pct = (participation.tauxParticipation * 100).roundToInt()
                Text(
                    text = "${participation.nbVotants} / ${participation.totalElecteurs} parents ont voté",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Couleurs.NoirEncre
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$pct % de participation",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Couleurs.GrisDoux
                )
                Spacer(Modifier.height(16.dp))
                BarreProgression(fraction = participation.tauxParticipation)
                Spacer(Modifier.height(16.dp))
                // Le message « aucun vote » dépend du nombre de votants (et non de
                // l'horodatage), pour rester cohérent avec le compteur ci-dessus.
                val dernier = participation.dernierVote
                val dernierMessage = when {
                    participation.nbVotants == 0 -> "Aucun vote pour l'instant."
                    dernier != null -> "Dernier vote à ${formatHeure(dernier)}"
                    else -> "Vote(s) enregistré(s)."
                }
                Text(
                    text = dernierMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = "Actualisé automatiquement toutes les 5 secondes.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Couleurs.GrisDoux
                )
            }
        }
    }
}

@Composable
private fun BarreProgression(fraction: Float) {
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Couleurs.NoirEncre.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(f)
                .height(14.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Couleurs.VertMenthe)
        )
    }
}

@Composable
private fun ActionsScrutin(
    statut: StatutScrutin,
    actionEnCours: Boolean,
    onOuvrir: () -> Unit,
    onFermer: () -> Unit,
    onDepouiller: () -> Unit
) {
    when (statut) {
        StatutScrutin.Configure -> BoutonClay(
            texte = if (actionEnCours) "OUVERTURE…" else "OUVRIR LE SCRUTIN AU VOTE",
            onClick = onOuvrir,
            modifier = Modifier.fillMaxWidth(),
            couleur = Couleurs.VertMenthe,
            enabled = !actionEnCours
        )
        StatutScrutin.Ouvert -> BoutonClay(
            texte = if (actionEnCours) "FERMETURE…" else "FERMER LE SCRUTIN",
            onClick = onFermer,
            modifier = Modifier.fillMaxWidth(),
            couleur = Couleurs.RougePompidou,
            enabled = !actionEnCours
        )
        StatutScrutin.Ferme -> BoutonClay(
            texte = if (actionEnCours) "DÉPOUILLEMENT…" else "DÉPOUILLER LE SCRUTIN",
            onClick = onDepouiller,
            modifier = Modifier.fillMaxWidth(),
            couleur = Couleurs.BleuKlein,
            enabled = !actionEnCours
        )
        StatutScrutin.Depouille -> Text(
            text = "Le scrutin a été dépouillé. Résultats ci-dessous.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux
        )
    }
}

@Composable
private fun ResultatsCard(
    resultats: ResultatScrutin,
    listes: List<ListeCandidate>,
    onExporterPv: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val nomListe: (String) -> String = { id -> listes.firstOrNull { it.id == id }?.nom ?: id }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, Couleurs.BleuKlein.copy(alpha = 0.25f), shape)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Résultats (algorithme de Hare)",
                style = MaterialTheme.typography.titleLarge,
                color = Couleurs.NoirEncre
            )
            Spacer(Modifier.height(12.dp))
            resultats.siegesAttribues.entries.sortedByDescending { it.value }.forEach { (listeId, sieges) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        text = nomListe(listeId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.NoirEncre,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${resultats.resultatsParListe[listeId] ?: 0} voix · $sieges siège(s)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            BoutonClay(
                texte = "Exporter le procès-verbal en PDF",
                onClick = onExporterPv,
                modifier = Modifier.fillMaxWidth(),
                couleur = Couleurs.VertMenthe
            )
        }
    }
}

@Composable
private fun CarteListe(
    liste: ListeCandidate,
    couleur: Color,
    configurable: Boolean,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(couleur, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (liste.nom.firstOrNull() ?: '?').uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = liste.nom,
                    style = MaterialTheme.typography.titleMedium,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = liste.candidats.joinToString(", ") { "${it.prenom} ${it.nom}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
            if (configurable) {
                ActionListe(libelle = "Modifier", couleur = Couleurs.BleuKlein, onClick = onModifier)
                Spacer(Modifier.size(4.dp))
                ActionListe(libelle = "Supprimer", couleur = Couleurs.RougePompidou, onClick = onSupprimer)
            } else {
                Text(
                    text = "${liste.candidats.size} cand.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Couleurs.GrisDoux
                )
            }
        }
    }
}

@Composable
private fun ActionListe(libelle: String, couleur: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(couleur.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = libelle, style = MaterialTheme.typography.labelLarge, color = couleur)
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

@Composable
private fun ListeVide(statut: StatutScrutin, onAjouterListe: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    ) {
        Text(
            text = "Aucune liste candidate pour l'instant.",
            style = MaterialTheme.typography.titleLarge,
            color = Couleurs.NoirEncre,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        if (statut == StatutScrutin.Configure) {
            Text(
                text = "Ajoutez les listes des parents avant d'ouvrir le scrutin.",
                style = MaterialTheme.typography.bodyLarge,
                color = Couleurs.GrisDoux,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            BoutonClay(
                texte = "+ Ajouter une liste",
                onClick = onAjouterListe,
                couleur = Couleurs.BleuKlein
            )
        } else {
            Text(
                text = "Le scrutin n'est plus en configuration, plus possible d'ajouter de liste.",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DialogueRenommer(
    nomActuel: String,
    onAnnuler: () -> Unit,
    onConfirmer: (String) -> Unit
) {
    var saisie by remember { mutableStateOf(nomActuel) }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Renommer le scrutin") },
        text = {
            Column {
                Text(
                    text = "Choisissez un nom plus lisible pour le dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
                Spacer(Modifier.height(12.dp))
                BasicTextField(
                    value = saisie,
                    onValueChange = { saisie = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = Couleurs.NoirEncre),
                    cursorBrush = SolidColor(Couleurs.BleuKlein),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, Couleurs.NoirEncre.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (saisie.isNotBlank()) onConfirmer(saisie) },
                enabled = saisie.isNotBlank()
            ) { Text("Renommer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun DialogueConfirmationSuppression(
    titre: String,
    corps: String,
    onAnnuler: () -> Unit,
    onConfirmer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(titre) },
        text = { Text(corps) },
        confirmButton = {
            TextButton(onClick = onConfirmer) {
                Text("Supprimer", color = Couleurs.RougePompidou)
            }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun Centre(message: String, spinner: Boolean) {
    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
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
    Box(modifier = Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
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

private fun statutDisplay(statut: StatutScrutin): Pair<Color, String> = when (statut) {
    StatutScrutin.Configure -> Couleurs.GrisDoux to "En configuration"
    StatutScrutin.Ouvert -> Couleurs.VertMenthe to "Ouvert au vote"
    StatutScrutin.Ferme -> Couleurs.JaunePop to "Fermé"
    StatutScrutin.Depouille -> Couleurs.BleuKlein to "Dépouillé"
}

private val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())
private fun formatDate(epochMillis: Long): String =
    if (epochMillis == Long.MAX_VALUE) "∞"
    else formatterDate.format(Instant.ofEpochMilli(epochMillis))

private val formatterHeure = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
private fun formatHeure(epochMillis: Long): String =
    formatterHeure.format(Instant.ofEpochMilli(epochMillis))
