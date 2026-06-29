package com.rayhanactis.voteparentseleves.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

// Lance l'activité de scan fournie par zxing-android-embedded (gère elle-même
// la permission caméra et l'UI de visée). Résultat renvoyé via le contrat
// AndroidX Activity Result, donc pas besoin d'Activity custom.
@Composable
actual fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ScanContract()) { resultat ->
        val contenu = resultat.contents
        if (contenu != null) onResultat(contenu) else onAnnuler()
    }
    LaunchedEffect(Unit) {
        launcher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setBeepEnabled(false)
                // false = on laisse l'activité (PortraitCaptureActivity) imposer
                // son orientation portrait via le manifeste.
                .setOrientationLocked(false)
                .setCaptureActivity(PortraitCaptureActivity::class.java)
                .setPrompt("Visez le QR code")
        )
    }
}
