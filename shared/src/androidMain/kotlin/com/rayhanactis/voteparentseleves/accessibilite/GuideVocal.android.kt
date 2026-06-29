package com.rayhanactis.voteparentseleves.accessibilite

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private class GuideVocalAndroid(private val tts: TextToSpeech) : GuideVocal {
    override fun lire(texte: String) {
        tts.speak(texte, TextToSpeech.QUEUE_FLUSH, null, "vpe-tts")
    }

    override fun arreter() {
        tts.stop()
    }
}

@Composable
actual fun rememberGuideVocal(): GuideVocal {
    val context = LocalContext.current
    val tts = remember { TextToSpeech(context, null).apply { language = Locale.FRENCH } }
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }
    return remember(tts) { GuideVocalAndroid(tts) }
}
