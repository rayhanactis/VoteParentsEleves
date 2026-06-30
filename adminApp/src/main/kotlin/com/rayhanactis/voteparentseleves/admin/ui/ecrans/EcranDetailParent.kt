package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.rayhanactis.voteparentseleves.admin.ui.composants.ColonneDefilante
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.DetailParentViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatDetailParent
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Electeur
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranDetailParent(token: String, parentId: String, onRetour: () -> Unit) {
    val api = LocalApiClient.current
    val vm: DetailParentViewModel = viewModel { DetailParentViewModel(api, token) }
    LaunchedEffect(parentId) { vm.charger(parentId) }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreRetour(onRetour)
            when (val etat = vm.etat) {
                EtatDetailParent.Chargement -> Centre("Chargement…", true)
                is EtatDetailParent.Erreur -> Centre(etat.message, false)
                is EtatDetailParent.Pret -> Formulaire(
                    parent = etat.parent,
                    motDePasseGenere = vm.motDePasseGenere,
                    message = vm.message,
                    actionEnCours = vm.actionEnCours,
                    onEnregistrer = { nom, prenom, email -> vm.modifier(parentId, nom, prenom, email) },
                    onGenerer = { vm.genererMotDePasse(parentId) },
                    onEnvoyer = { vm.envoyerParMail(parentId) }
                )
            }
        }
    }
}

@Composable
private fun BarreRetour(onRetour: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 2.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRetour() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("← Retour au répertoire", style = MaterialTheme.typography.labelLarge, color = Couleurs.BleuKlein)
        }
    }
}

@Composable
private fun Formulaire(
    parent: Electeur,
    motDePasseGenere: String?,
    message: String?,
    actionEnCours: Boolean,
    onEnregistrer: (String, String, String) -> Unit,
    onGenerer: () -> Unit,
    onEnvoyer: () -> Unit
) {
    var nom by remember(parent.id) { mutableStateOf(parent.nom) }
    var prenom by remember(parent.id) { mutableStateOf(parent.prenom) }
    var email by remember(parent.id) { mutableStateOf(parent.email) }

    ColonneDefilante(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CartePleine(modifier = Modifier.widthIn(max = 640.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Informations du parent",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Couleurs.NoirEncre
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ChampTexte(prenom, { prenom = it }, "Prénom")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ChampTexte(nom, { nom = it }, "Nom")
                    }
                }
                ChampTexte(email, { email = it }, "Adresse email", placeholder = "ex : alice.dupont@email.fr")
                BoutonClay(
                    texte = "Enregistrer les modifications",
                    onClick = { onEnregistrer(nom, prenom, email) },
                    modifier = Modifier.fillMaxWidth(),
                    couleur = Couleurs.BleuKlein,
                    enabled = nom.isNotBlank() && prenom.isNotBlank()
                )

                Spacer(Modifier.height(8.dp))
                SectionIdentifiantsParent(
                    identifiant = parent.id,
                    motDePasseGenere = motDePasseGenere,
                    emailEnregistre = parent.email,
                    actionEnCours = actionEnCours,
                    onGenerer = onGenerer,
                    onEnvoyer = onEnvoyer
                )

                if (message != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Couleurs.BleuKlein.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(message, color = Couleurs.BleuKlein, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionIdentifiantsParent(
    identifiant: String,
    motDePasseGenere: String?,
    emailEnregistre: String,
    actionEnCours: Boolean,
    onGenerer: () -> Unit,
    onEnvoyer: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Couleurs.FondCreme, shape)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "Identifiants de vote",
                style = MaterialTheme.typography.titleLarge,
                color = Couleurs.NoirEncre
            )
            Text("Identifiant : $identifiant", style = MaterialTheme.typography.bodyLarge, color = Couleurs.NoirEncre)
            Text(
                text = "Mot de passe : " + (motDePasseGenere ?: "•••••• (non affichable — générez pour le voir)"),
                style = MaterialTheme.typography.bodyLarge,
                color = if (motDePasseGenere != null) Couleurs.NoirEncre else Couleurs.GrisDoux
            )
            Text(
                "Le mot de passe n'est pas conservé en clair : il n'est visible qu'au moment de sa génération. " +
                    "« Envoyer par email » régénère un mot de passe et l'envoie au parent.",
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.GrisDoux
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BoutonClay(
                    texte = if (actionEnCours) "…" else "Générer le mot de passe",
                    onClick = onGenerer,
                    modifier = Modifier.weight(1f),
                    couleur = Couleurs.BleuKlein,
                    enabled = !actionEnCours,
                    style = MaterialTheme.typography.labelMedium
                )
                BoutonClay(
                    texte = if (actionEnCours) "…" else "Envoyer par email",
                    onClick = onEnvoyer,
                    modifier = Modifier.weight(1f),
                    couleur = Couleurs.VertMenthe,
                    enabled = !actionEnCours && emailEnregistre.isNotBlank(),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (emailEnregistre.isBlank()) {
                Text(
                    "Renseignez puis enregistrez une adresse email pour pouvoir l'envoyer.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Couleurs.RougePompidou
                )
            }
        }
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
