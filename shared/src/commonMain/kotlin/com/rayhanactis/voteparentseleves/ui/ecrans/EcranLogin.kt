package com.rayhanactis.voteparentseleves.ui.ecrans

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
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.BoutonLireVoixHaute
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.mock.MockData
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import com.rayhanactis.voteparentseleves.viewmodel.EtatLogin
import com.rayhanactis.voteparentseleves.viewmodel.LoginViewModel

@Composable
fun EcranLogin(
    scrutinId: String,
    onConnecte: (token: String, scrutinId: String, dejaVote: Boolean) -> Unit
) {
    val api = LocalApiClient.current
    val vm: LoginViewModel = viewModel { LoginViewModel(api) }

    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 460.dp)) {
                FormulaireLogin(
                    vm = vm,
                    scrutinId = scrutinId,
                    onConnecte = onConnecte
                )
            }
        }
    }
}

@Composable
private fun FormulaireLogin(
    vm: LoginViewModel,
    scrutinId: String,
    onConnecte: (String, String, Boolean) -> Unit
) {
    var code by remember { mutableStateOf("parent1") }
    var motDePasse by remember { mutableStateOf("0000") }

    val etat = vm.etat
    val chargement = etat is EtatLogin.Chargement

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Je vote",
            style = MaterialTheme.typography.displayLarge,
            color = Couleurs.NoirEncre
        )
        Text(
            text = MockData.ECOLE_NOM,
            style = MaterialTheme.typography.bodyLarge,
            color = Couleurs.GrisDoux
        )

        BoutonLireVoixHaute(
            texte = "Écran de connexion. Entrez votre code et votre mot de passe pour voter à l'élection des parents d'élèves."
        )

        BadgeDemo()

        ChampTexte(
            valeur = code,
            onChange = { code = it; vm.resetErreur() },
            label = "Mon code",
            placeholder = "Ex : parent1",
            typeClavier = KeyboardType.Text
        )
        ChampTexte(
            valeur = motDePasse,
            onChange = { motDePasse = it; vm.resetErreur() },
            label = "Mon mot de passe",
            placeholder = "••••",
            motDePasse = true
        )

        if (etat is EtatLogin.Erreur) {
            MessageErreur(etat.message)
        }

        Spacer(Modifier.height(4.dp))

        BoutonClay(
            texte = if (chargement) "CONNEXION…" else "ENTRER  →",
            onClick = {
                vm.seConnecter(
                    code = code,
                    motDePasse = motDePasse,
                    scrutinId = scrutinId,
                    onSucces = onConnecte
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !chargement
        )

        Text(
            text = "Votre vote est anonyme et sécurisé.",
            style = MaterialTheme.typography.bodyMedium,
            color = Couleurs.GrisDoux
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
            text = "Démo : parent1 … parent5  /  0000",
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
