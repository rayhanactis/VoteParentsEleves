package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVSpeechBoundary
import platform.AVFoundation.AVSpeechSynthesisVoice
import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechUtterance

private class GuideVocalIos(private val synthetiseur: AVSpeechSynthesizer) : GuideVocal {
    override fun lire(texte: String) {
        synthetiseur.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        val enonce = AVSpeechUtterance(string = texte)
        enonce.voice = AVSpeechSynthesisVoice.voiceWithLanguage("fr-FR")
        synthetiseur.speakUtterance(enonce)
    }

    override fun arreter() {
        synthetiseur.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}

@Composable
actual fun rememberGuideVocal(): GuideVocal = remember { GuideVocalIos(AVSpeechSynthesizer()) }
