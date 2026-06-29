package com.rayhanactis.voteparentseleves.ui.composants

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

// Fond plein écran dans la veine Kandinsky / Pompidou : aplats colorés,
// formes géométriques primaires positionnées en composition libre.
// Statique pour cette première passe.
@Composable
fun FondKandinsky(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(Couleurs.FondCreme)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Gros cercle rouge en haut à gauche, légèrement coupé
            drawCircle(
                color = Couleurs.RougePompidou.copy(alpha = 0.85f),
                radius = w * 0.28f,
                center = Offset(x = w * 0.05f, y = h * 0.08f)
            )

            // Carré bleu pivoté en haut à droite
            rotate(degrees = 18f, pivot = Offset(w * 0.85f, h * 0.18f)) {
                drawRect(
                    color = Couleurs.BleuKlein.copy(alpha = 0.9f),
                    topLeft = Offset(w * 0.72f, h * 0.06f),
                    size = Size(w * 0.30f, w * 0.30f)
                )
            }

            // Triangle jaune en bas à gauche
            val triangle = Path().apply {
                moveTo(w * 0.04f, h * 0.95f)
                lineTo(w * 0.42f, h * 0.95f)
                lineTo(w * 0.22f, h * 0.65f)
                close()
            }
            drawPath(triangle, color = Couleurs.JaunePop)

            // Cercle vert moyen en bas à droite
            drawCircle(
                color = Couleurs.VertMenthe.copy(alpha = 0.85f),
                radius = w * 0.18f,
                center = Offset(x = w * 0.88f, y = h * 0.88f)
            )

            // Petit point rose central
            drawCircle(
                color = Couleurs.RosePop,
                radius = w * 0.04f,
                center = Offset(x = w * 0.62f, y = h * 0.55f)
            )

            // Ligne noire épaisse oblique
            drawLine(
                color = Couleurs.NoirEncre,
                start = Offset(w * 0.15f, h * 0.45f),
                end = Offset(w * 0.55f, h * 0.32f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Ligne pointillée jaune-noire
            drawLine(
                color = Couleurs.NoirEncre,
                start = Offset(w * 0.65f, h * 0.78f),
                end = Offset(w * 0.95f, h * 0.62f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f))
            )

            // Cercle évidé orange
            drawCircle(
                color = Couleurs.OrangeCh,
                radius = w * 0.08f,
                center = Offset(x = w * 0.35f, y = h * 0.72f),
                style = Stroke(width = 6.dp.toPx())
            )
        }
    }
}
