package com.rayhanactis.voteparentseleves.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorSchemePop = lightColorScheme(
    primary = Couleurs.BleuKlein,
    onPrimary = Couleurs.BlancCasse,
    secondary = Couleurs.RougePompidou,
    onSecondary = Couleurs.BlancCasse,
    tertiary = Couleurs.JaunePop,
    onTertiary = Couleurs.NoirEncre,
    background = Couleurs.FondCreme,
    onBackground = Couleurs.NoirEncre,
    surface = Couleurs.BlancCasse,
    onSurface = Couleurs.NoirEncre,
    error = Couleurs.RougePompidou,
    onError = Couleurs.BlancCasse
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorSchemePop,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
