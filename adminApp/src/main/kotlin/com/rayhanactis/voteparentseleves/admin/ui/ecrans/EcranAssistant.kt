package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.ui.composants.BarreNavigation
import com.rayhanactis.voteparentseleves.admin.ui.composants.LazyColonneDefilante
import com.rayhanactis.voteparentseleves.admin.ui.composants.OngletAdmin
import com.rayhanactis.voteparentseleves.admin.viewmodel.AssistantViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.MessageAssistant
import com.rayhanactis.voteparentseleves.admin.viewmodel.RoleMessage
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranAssistant(
    onAllerDashboard: () -> Unit,
    onAllerRepertoire: () -> Unit,
    onAllerParametres: () -> Unit,
    onDeconnexion: () -> Unit
) {
    val vm: AssistantViewModel = viewModel { AssistantViewModel() }
    val listState = rememberLazyListState()

    LaunchedEffect(vm.messages.size, vm.enCours) {
        val dernier = vm.messages.size - 1 + if (vm.enCours) 1 else 0
        if (dernier >= 0) listState.animateScrollToItem(dernier)
    }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigation(
                actif = OngletAdmin.ASSISTANT,
                onAllerDashboard = onAllerDashboard,
                onAllerRepertoire = onAllerRepertoire,
                onAllerParametres = onAllerParametres,
                onAllerAssistant = {},
                onDeconnexion = onDeconnexion
            )

            Entete(modele = vm.modele, onModele = vm::majModele)

            LazyColonneDefilante(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(vm.messages) { message -> Bulle(message) }
                if (vm.enCours) {
                    item { BulleChargement() }
                }
            }

            ZoneSaisie(
                valeur = vm.saisie,
                enCours = vm.enCours,
                onValeur = vm::majSaisie,
                onEnvoyer = vm::envoyer
            )
        }
    }
}

@Composable
private fun Entete(modele: String, onModele: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Couleurs.BleuKlein.copy(alpha = 0.06f))
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Assistant d'aide — répond à partir du manuel de l'application. " +
                "Vérifiez les informations importantes ; il ne déclenche aucune action.",
            style = MaterialTheme.typography.bodySmall,
            color = Couleurs.GrisDoux,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.height(0.dp))
        OutlinedTextField(
            value = modele,
            onValueChange = onModele,
            label = { Text("Modèle Ollama") },
            singleLine = true,
            modifier = Modifier.widthIn(min = 180.dp)
        )
    }
}

@Composable
private fun Bulle(message: MessageAssistant) {
    val estUtilisateur = message.role == RoleMessage.UTILISATEUR
    val estErreur = message.role == RoleMessage.ERREUR
    val fond = when {
        estUtilisateur -> Couleurs.BleuKlein
        estErreur -> Couleurs.RougePompidou.copy(alpha = 0.12f)
        else -> Color.White
    }
    val couleurTexte = when {
        estUtilisateur -> Color.White
        estErreur -> Couleurs.RougePompidou
        else -> Couleurs.NoirEncre
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (estUtilisateur) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(fond, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.texte,
                style = MaterialTheme.typography.bodyLarge,
                color = couleurTexte
            )
        }
    }
}

@Composable
private fun BulleChargement() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = Couleurs.BleuKlein,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(18.dp).widthIn(min = 18.dp)
                )
                Spacer(Modifier.widthIn(min = 12.dp))
                Text(
                    text = "L'assistant réfléchit…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Couleurs.GrisDoux
                )
            }
        }
    }
}

@Composable
private fun ZoneSaisie(
    valeur: String,
    enCours: Boolean,
    onValeur: (String) -> Unit,
    onEnvoyer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = valeur,
            onValueChange = onValeur,
            placeholder = { Text("Posez votre question…") },
            modifier = Modifier.weight(1f),
            enabled = !enCours,
            maxLines = 4
        )
        Spacer(Modifier.widthIn(min = 12.dp))
        BoutonClay(
            texte = if (enCours) "…" else "Envoyer",
            onClick = onEnvoyer,
            couleur = Couleurs.BleuKlein,
            enabled = !enCours && valeur.isNotBlank()
        )
    }
}
