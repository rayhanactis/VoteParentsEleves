package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object GuideVocalDesactive : GuideVocal {
    override fun lire(texte: String) = Unit
    override fun arreter() = Unit
}

// Démo navigateur : pas de TTS câblé (Web Speech API non utilisée ici).
@Composable
actual fun rememberGuideVocal(): GuideVocal = remember { GuideVocalDesactive }
