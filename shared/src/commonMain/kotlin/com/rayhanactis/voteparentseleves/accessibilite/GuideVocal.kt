package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable

interface GuideVocal {
    fun lire(texte: String)
    fun arreter()
}

@Composable
expect fun rememberGuideVocal(): GuideVocal
