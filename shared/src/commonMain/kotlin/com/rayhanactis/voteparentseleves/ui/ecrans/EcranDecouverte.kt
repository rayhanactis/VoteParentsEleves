package com.rayhanactis.voteparentseleves.ui.ecrans

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
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.qr.QrPayload
import com.rayhanactis.voteparentseleves.qr.parserQrPayload
import com.rayhanactis.voteparentseleves.scan.ScannerQrPlein
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranDecouverte(
    baseUrlDemo: String,
    onBaseUrlChoisie: (String) -> Unit
) {
    var enScan by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var saisieManuelle by remember { mutableStateOf("") }

    if (enScan) {
        ScannerQrPlein(
            onResultat = { brut ->
                enScan = false
                when (val payload = parserQrPayload(brut)) {
                    is QrPayload.DecouverteServeur -> onBaseUrlChoisie(payload.baseUrl)
                    else -> erreur = "QR non reconnu. Demandez à l'école de le projeter à nouveau."
                }
            },
            onAnnuler = { enScan = false },
            onIndisponible = {
                enScan = false
                erreur = "Scanner indisponible sur cet appareil. Saisissez l'adresse ci-dessous."
            }
        )
        return
    }

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
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Connexion au vote",
                        style = MaterialTheme.typography.displayLarge,
                        color = Couleurs.NoirEncre
                    )
                    Text(
                        text = "Scannez le QR projeté par l'école pour rejoindre son réseau de vote.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux
                    )

                    if (erreur != null) {
                        Text(
                            text = erreur ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Couleurs.RougePompidou
                        )
                    }

                    BoutonClay(
                        texte = "Scanner le QR de l'école",
                        onClick = { erreur = null; enScan = true },
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.BleuKlein
                    )

                    Text(
                        text = "Ou saisissez l'adresse manuellement (ex : http://192.168.1.42:8080) :",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Couleurs.GrisDoux
                    )
                    ChampTexte(
                        valeur = saisieManuelle,
                        onChange = { saisieManuelle = it },
                        label = "Adresse du serveur",
                        placeholder = "http://192.168.1.42:8080"
                    )
                    BoutonClay(
                        texte = "Se connecter",
                        onClick = { if (saisieManuelle.isNotBlank()) onBaseUrlChoisie(saisieManuelle.trim()) },
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.VertMenthe,
                        enabled = saisieManuelle.isNotBlank()
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Démo : utiliser le serveur de test ($baseUrlDemo)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Couleurs.GrisDoux,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BoutonClay(
                        texte = "Utiliser le serveur de démonstration",
                        onClick = { onBaseUrlChoisie(baseUrlDemo) },
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.GrisDoux
                    )
                }
            }
        }
    }
}
