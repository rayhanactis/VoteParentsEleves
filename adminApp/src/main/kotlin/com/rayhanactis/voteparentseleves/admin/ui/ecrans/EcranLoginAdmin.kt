package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatLoginAdmin
import com.rayhanactis.voteparentseleves.admin.viewmodel.LoginAdminViewModel
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranLoginAdmin(onConnecte: (token: String) -> Unit) {
    val api = LocalApiClient.current
    val vm: LoginAdminViewModel = viewModel { LoginAdminViewModel(api) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Couleurs.FondCreme)
    ) {
        ColonneDefilante(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 480.dp)) {
                Formulaire(vm = vm, onConnecte = onConnecte)
            }
        }
    }
}

@Composable
private fun Formulaire(
    vm: LoginAdminViewModel,
    onConnecte: (String) -> Unit
) {
    var code by remember { mutableStateOf("admin") }
    var motDePasse by remember { mutableStateOf("admin123") }
    val etat = vm.etat
    val chargement = etat is EtatLoginAdmin.Chargement

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Espace administration",
            style = MaterialTheme.typography.displaySmall,
            color = Couleurs.NoirEncre
        )
        Text(
            text = "Gestion des scrutins de votre école.",
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux
        )

        BadgeDemo()

        ChampTexte(
            valeur = code,
            onChange = { code = it; vm.resetErreur() },
            label = "Identifiant administrateur",
            placeholder = "Ex : admin",
            typeClavier = KeyboardType.Text
        )
        ChampTexte(
            valeur = motDePasse,
            onChange = { motDePasse = it; vm.resetErreur() },
            label = "Mot de passe",
            placeholder = "••••••••",
            motDePasse = true
        )

        if (etat is EtatLoginAdmin.Erreur) {
            MessageErreur(etat.message)
        }

        Spacer(Modifier.height(4.dp))

        BoutonClay(
            texte = if (chargement) "CONNEXION…" else "ENTRER  →",
            onClick = { vm.seConnecter(code, motDePasse, onConnecte) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !chargement
        )
    }
}

@Composable
private fun BadgeDemo() {
    Box(
        modifier = Modifier
            .background(color = Couleurs.JaunePop, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Démo : admin / admin123",
            style = MaterialTheme.typography.labelMedium,
            color = Couleurs.NoirEncre
        )
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
