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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.ui.composants.BarreNavigation
import com.rayhanactis.voteparentseleves.admin.ui.composants.OngletAdmin
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatEnvoiTous
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatRepertoire
import com.rayhanactis.voteparentseleves.admin.viewmodel.RepertoireViewModel
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Electeur
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranRepertoire(
    token: String,
    onAllerDashboard: () -> Unit,
    onAllerParametres: () -> Unit,
    onDeconnexion: () -> Unit,
    onOuvrirParent: (parentId: String) -> Unit
) {
    val api = LocalApiClient.current
    val vm: RepertoireViewModel = viewModel { RepertoireViewModel(api, token) }
    LaunchedEffect(Unit) { vm.charger() }

    var dialogNouveau by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigation(
                actif = OngletAdmin.REPERTOIRE,
                onAllerDashboard = onAllerDashboard,
                onAllerRepertoire = { vm.charger() },
                onAllerParametres = onAllerParametres,
                onActualiser = { vm.charger() },
                onDeconnexion = onDeconnexion
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 1000.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Répertoire des parents d'élèves",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Couleurs.NoirEncre
                        )
                        Text(
                            text = "Liste de tous les parents. Source unique pour la sélection des candidats.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Couleurs.GrisDoux
                        )
                    }
                    BoutonClay("+ Nouveau parent", { dialogNouveau = true }, couleur = Couleurs.BleuKlein)
                }

                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1000.dp)) {
                    SectionEnvoiTous(
                        enCours = vm.etatEnvoiTous is EtatEnvoiTous.EnCours,
                        onEnvoyer = { vm.envoyerATous() }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 1000.dp)) {
                    ChampTexte(
                        valeur = vm.recherche,
                        onChange = { vm.recherche = it },
                        label = "Rechercher (nom, prénom, email ou identifiant)",
                        placeholder = "ex : Dupont"
                    )
                }

                Spacer(Modifier.height(16.dp))

                when (val etat = vm.etat) {
                    EtatRepertoire.Chargement -> Centre("Chargement…", true)
                    is EtatRepertoire.Erreur -> CentreErreur(etat.message) { vm.charger() }
                    is EtatRepertoire.Pret -> {
                        val parents = vm.parentsFiltres()
                        if (parents.isEmpty() && vm.recherche.isBlank()) {
                            RepertoireVide { dialogNouveau = true }
                        } else if (parents.isEmpty()) {
                            Text(
                                "Aucun parent ne correspond à « ${vm.recherche} ».",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Couleurs.GrisDoux
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().widthIn(max = 1000.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(parents) { parent ->
                                    CarteParent(
                                        parent,
                                        { onOuvrirParent(parent.id) },
                                        { vm.supprimerParent(parent.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogNouveau) {
        DialogueNouveauParent(
            onAnnuler = { dialogNouveau = false },
            onCreer = { nom, prenom, email ->
                dialogNouveau = false
                vm.creerParent(nom, prenom, email) { }
            }
        )
    }

    DialogueEnvoiTous(
        etat = vm.etatEnvoiTous,
        onFermer = { vm.fermerEnvoiTous() }
    )
}

@Composable
private fun SectionEnvoiTous(enCours: Boolean, onEnvoyer: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.5.dp, Couleurs.NoirEncre.copy(alpha = 0.06f), shape)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Identifiants de vote",
                    style = MaterialTheme.typography.titleLarge,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = "Génère un nouveau mot de passe pour chaque parent disposant d'une adresse " +
                        "email et le lui envoie avec son identifiant.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
            BoutonClay(
                texte = if (enCours) "Envoi…" else "Envoyer les identifiants à tous",
                onClick = onEnvoyer,
                couleur = Couleurs.VertMenthe,
                enabled = !enCours,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DialogueEnvoiTous(etat: EtatEnvoiTous, onFermer: () -> Unit) {
    when (etat) {
        EtatEnvoiTous.Inactif -> Unit
        EtatEnvoiTous.EnCours -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Envoi des identifiants en cours…") },
            text = { CircularProgressIndicator(color = Couleurs.BleuKlein) },
            confirmButton = {}
        )
        is EtatEnvoiTous.Termine -> AlertDialog(
            onDismissRequest = onFermer,
            title = { Text("Envoi terminé") },
            text = {
                Column {
                    Text("${etat.bilan.envoyes} email(s) envoyé(s).", style = MaterialTheme.typography.bodyMedium)
                    if (etat.bilan.sansEmail > 0) {
                        Text(
                            "${etat.bilan.sansEmail} parent(s) sans adresse email (ignoré(s)).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Couleurs.GrisDoux
                        )
                    }
                    if (etat.bilan.echecs > 0) {
                        Text(
                            "${etat.bilan.echecs} échec(s) d'envoi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Couleurs.RougePompidou
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = onFermer) { Text("OK") } }
        )
        is EtatEnvoiTous.Erreur -> AlertDialog(
            onDismissRequest = onFermer,
            title = { Text("Erreur") },
            text = { Text(etat.message, color = Couleurs.RougePompidou) },
            confirmButton = { TextButton(onClick = onFermer) { Text("OK") } }
        )
    }
}

@Composable
private fun CarteParent(parent: Electeur, onOuvrir: () -> Unit, onSupprimer: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = shape)
            .background(Color.White, shape)
            .border(1.dp, Couleurs.NoirEncre.copy(alpha = 0.06f), shape)
            .clickable { onOuvrir() }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(Couleurs.BleuKlein, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${parent.prenom.firstOrNull() ?: ""}${parent.nom.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${parent.prenom} ${parent.nom}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Couleurs.NoirEncre
                )
                Text(
                    text = parent.email.ifBlank { "${parent.id} · pas d'email" }
                        .let { if (parent.email.isNotBlank()) "${parent.id} · ${parent.email}" else it },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
            Box(
                modifier = Modifier
                    .background(Couleurs.RougePompidou.copy(alpha = 0.10f), CircleShape)
                    .clickable { onSupprimer() }
                    .padding(10.dp)
            ) {
                Text(
                    "Supprimer",
                    style = MaterialTheme.typography.labelLarge,
                    color = Couleurs.RougePompidou
                )
            }
        }
    }
}

@Composable
private fun DialogueNouveauParent(onAnnuler: () -> Unit, onCreer: (String, String, String) -> Unit) {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Nouveau parent") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChampTexte(prenom, { prenom = it }, "Prénom", placeholder = "ex : Alice")
                ChampTexte(nom, { nom = it }, "Nom", placeholder = "ex : Dupont")
                ChampTexte(email, { email = it }, "Email (pour l'envoi des identifiants)", placeholder = "ex : alice.dupont@email.fr")
            }
        },
        confirmButton = {
            TextButton(
                enabled = nom.isNotBlank() && prenom.isNotBlank(),
                onClick = { onCreer(nom, prenom, email) }
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onAnnuler) { Text("Annuler") } }
    )
}

@Composable
private fun RepertoireVide(onNouveau: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Aucun parent dans le répertoire.",
            style = MaterialTheme.typography.titleLarge,
            color = Couleurs.NoirEncre,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Commencez par ajouter les parents de l'école.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        BoutonClay("+ Ajouter un premier parent", onNouveau, couleur = Couleurs.BleuKlein)
    }
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
            Text(message, style = MaterialTheme.typography.titleMedium, color = Couleurs.RougePompidou)
            Spacer(Modifier.height(16.dp))
            BoutonClay("Réessayer", onReessayer, Modifier.fillMaxWidth(), Couleurs.BleuKlein)
        }
    }
}
