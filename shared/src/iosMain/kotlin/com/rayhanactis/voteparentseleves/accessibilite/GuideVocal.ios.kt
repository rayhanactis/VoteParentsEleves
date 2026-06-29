package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVSpeechBoundary
import platform.AVFoundation.AVSpeechSynthesisVoice
import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechUtterance

// Best-effort : développé/écrit sans Mac/Xcode disponible dans cet
// environnement, donc non compilé/testé localement. AVFoundation fait
// partie des frameworks système exposés nativement par Kotlin/Native
// (même famille que `platform.UIKit` déjà utilisé dans Platform.ios.kt),
// donc cette implémentation ne devrait pas nécessiter de config cinterop
// supplémentaire — à valider lors d'un prochain build sur Mac.
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
