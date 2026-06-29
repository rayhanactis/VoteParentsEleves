package com.rayhanactis.voteparentseleves.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// Scan caméra iOS (AVFoundation) non câblé dans cette passe : nécessite
// Xcode/un Mac pour développer et valider l'interop Objective-C, non
// disponible dans cet environnement de build Windows. On retombe sur la
// saisie manuelle du code+mot de passe (toujours fonctionnelle), qui reste
// le scénario nominal documenté pour iOS jusqu'à la prochaine itération.
@Composable
actual fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
) {
    LaunchedEffect(Unit) { onIndisponible() }
}
