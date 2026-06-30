package com.rayhanactis.voteparentseleves.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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
                .setOrientationLocked(false)
                .setCaptureActivity(PortraitCaptureActivity::class.java)
                .setPrompt("Visez le QR code")
        )
    }
}
