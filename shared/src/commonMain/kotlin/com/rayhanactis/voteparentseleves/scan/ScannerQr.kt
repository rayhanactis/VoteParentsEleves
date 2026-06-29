package com.rayhanactis.voteparentseleves.scan

import androidx.compose.runtime.Composable

// Scan plein écran d'un QR code. L'implémentation Android utilise la
// caméra (zxing-android-embedded) ; sur les plateformes sans scanner
// disponible, l'implémentation actual appelle directement onIndisponible
// pour que l'appelant retombe sur la saisie manuelle.
@Composable
expect fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
)
