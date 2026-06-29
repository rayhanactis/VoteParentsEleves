package com.rayhanactis.voteparentseleves.accessibilite

import androidx.compose.runtime.Composable

// Accessibilité parents malvoyants (ROADMAP Phase 4) : lecture à voix haute
// du contenu de l'écran. Pas d'effet de bord dans les ViewModels — le guide
// vocal est un service d'UI pur, instancié et possédé par la composition.
interface GuideVocal {
    fun lire(texte: String)
    fun arreter()
}

@Composable
expect fun rememberGuideVocal(): GuideVocal
