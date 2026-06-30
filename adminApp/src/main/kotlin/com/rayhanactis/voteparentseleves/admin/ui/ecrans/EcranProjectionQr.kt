package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.admin.qr.genererQrImageBitmap
import com.rayhanactis.voteparentseleves.admin.reseau.adresseIpLocale
import com.rayhanactis.voteparentseleves.qr.construireQrDecouverte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranProjectionQr(port: Int, onRetour: () -> Unit) {
    val adresseIp = remember { adresseIpLocale() }
    val baseUrl = "http://${adresseIp ?: "adresse-ip-introuvable"}:$port"
    val contenuQr = remember(baseUrl) { construireQrDecouverte(baseUrl) }
    val bitmap = remember(contenuQr) { genererQrImageBitmap(contenuQr, taillePx = 600) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Couleurs.NoirEncre)
            .clickable { onRetour() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Connexion au vote",
                style = MaterialTheme.typography.displayMedium,
                color = Couleurs.BlancCasse
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .background(Couleurs.BlancCasse, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Image(bitmap = bitmap, contentDescription = "QR code de connexion au serveur de vote", modifier = Modifier.size(420.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (adresseIp != null) {
                    "Scannez ce QR avec l'application de vote pour rejoindre le réseau de l'école."
                } else {
                    "Adresse réseau introuvable. Vérifiez la connexion Wi-Fi du poste."
                },
                style = MaterialTheme.typography.titleMedium,
                color = Couleurs.BlancCasse
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = baseUrl,
                style = MaterialTheme.typography.bodyLarge,
                color = Couleurs.GrisDoux
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Touchez l'écran pour revenir à l'administration",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )
        }
    }
}
