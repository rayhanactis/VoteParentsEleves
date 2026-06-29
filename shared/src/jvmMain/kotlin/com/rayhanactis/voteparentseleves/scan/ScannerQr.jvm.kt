package com.rayhanactis.voteparentseleves.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// Pas de caméra pertinente pour la cible JVM (démo desktop/dev) : on
// retombe immédiatement sur la saisie manuelle côté appelant.
@Composable
actual fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
) {
    LaunchedEffect(Unit) { onIndisponible() }
}
