package com.rayhanactis.voteparentseleves.scan

import androidx.compose.runtime.Composable

@Composable
expect fun ScannerQrPlein(
    onResultat: (String) -> Unit,
    onAnnuler: () -> Unit,
    onIndisponible: () -> Unit
)
