package com.rayhanactis.voteparentseleves.ui.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Carte glassmorphique (faux verre dépoli) : fond blanc semi-transparent
// + bordure claire + ombre douce. Sans backdrop-blur natif (pour rester
// compatible Wasm/anciennes plateformes), on s'appuie sur la composition
// sur fond coloré pour faire passer la sensation "verre".
@Composable
fun CarteGlass(
    modifier: Modifier = Modifier,
    couleurAccent: Color? = null,
    contenuPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(28.dp),
    contenu: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    val degradeFond = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.78f),
            Color.White.copy(alpha = 0.55f)
        )
    )

    Box(
        modifier = modifier
            .shadow(elevation = 18.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.10f))
            .clip(shape)
            .background(degradeFond)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
            .padding(contenuPadding)
    ) {
        if (couleurAccent != null) {
            // Petite barre d'accent en haut, marque la liste / l'écran
            Box(
                modifier = Modifier
                    .background(couleurAccent, shape = RoundedCornerShape(percent = 50))
            )
        }
        contenu()
    }
}

// Variante surface "pleine" plus opaque (utile pour mettre du contenu
// par-dessus le fond Kandinsky sans tout voir au travers).
@Composable
fun CartePleine(
    modifier: Modifier = Modifier,
    contenu: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 20.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.10f))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .border(2.dp, Color.White.copy(alpha = 0.8f), shape)
            .padding(28.dp)
    ) {
        contenu()
    }
}
