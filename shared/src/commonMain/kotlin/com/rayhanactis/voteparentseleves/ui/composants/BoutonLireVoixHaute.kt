package com.rayhanactis.voteparentseleves.ui.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.accessibilite.rememberGuideVocal
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

// Bouton "lire à voix haute" présent sur chaque écran du parcours vote
// (accessibilité parents malvoyants, ROADMAP Phase 4). Volontairement
// déclenché manuellement plutôt qu'auto-annoncé : pas de détection fiable
// et multiplateforme de TalkBack/VoiceOver à ce stade.
@Composable
fun BoutonLireVoixHaute(texte: String, modifier: Modifier = Modifier) {
    val guideVocal = rememberGuideVocal()
    Box(
        modifier = modifier
            .background(Couleurs.NoirEncre.copy(alpha = 0.06f), CircleShape)
            .clickable { guideVocal.lire(texte) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text("Lire à voix haute", style = MaterialTheme.typography.labelLarge, color = Couleurs.NoirEncre)
    }
}
