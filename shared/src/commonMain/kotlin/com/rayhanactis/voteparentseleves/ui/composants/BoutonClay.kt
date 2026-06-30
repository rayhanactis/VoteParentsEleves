package com.rayhanactis.voteparentseleves.ui.composants

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun BoutonClay(
    texte: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    couleur: Color = MaterialTheme.colorScheme.primary,
    couleurContenu: Color = Couleurs.BlancCasse,
    enabled: Boolean = true,
    style: TextStyle? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val presse by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (presse) 4.dp else 14.dp,
        animationSpec = tween(120)
    )
    val translation by animateFloatAsState(
        targetValue = if (presse) 2f else 0f,
        animationSpec = tween(120)
    )
    val shape = RoundedCornerShape(28.dp)

    val gradient = Brush.linearGradient(
        colors = listOf(
            couleur.melangerAvec(Color.White, 0.12f),
            couleur,
            couleur.melangerAvec(Color.Black, 0.10f)
        )
    )

    Box(
        modifier = modifier
            .sizeIn(minHeight = 72.dp)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = couleur.copy(alpha = 0.6f),
                spotColor = couleur.copy(alpha = 0.4f)
            )
            .background(gradient, shape)
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(PaddingValues(horizontal = 20.dp, vertical = 22.dp)),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides couleurContenu) {
            ProvideTextStyle(style ?: MaterialTheme.typography.labelLarge) {
                Text(
                    text = texte,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun Color.melangerAvec(autre: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = red + (autre.red - red) * r,
        green = green + (autre.green - green) * r,
        blue = blue + (autre.blue - blue) * r,
        alpha = alpha
    )
}
