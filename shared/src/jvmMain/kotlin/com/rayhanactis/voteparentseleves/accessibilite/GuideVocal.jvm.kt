package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object GuideVocalDesactive : GuideVocal {
    override fun lire(texte: String) = Unit
    override fun arreter() = Unit
}

// Pas de TTS embarqué pertinent pour la cible JVM (démo desktop/dev).
@Composable
actual fun rememberGuideVocal(): GuideVocal = remember { GuideVocalDesactive }
