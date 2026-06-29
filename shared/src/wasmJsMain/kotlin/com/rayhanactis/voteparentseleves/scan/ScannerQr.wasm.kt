package com.rayhanactis.voteparentseleves.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

// Démo navigateur (Wasm) : pas d'accès caméra natif câblé, on retombe sur
// la saisie manuelle.
@Composable
actual fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
) {
    LaunchedEffect(Unit) { onIndisponible() }
}
