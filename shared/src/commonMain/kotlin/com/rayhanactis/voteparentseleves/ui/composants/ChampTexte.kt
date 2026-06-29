package com.rayhanactis.voteparentseleves.ui.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun ChampTexte(
    valeur: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    motDePasse: Boolean = false,
    typeClavier: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Couleurs.NoirEncre
        )
        BasicTextField(
            value = valeur,
            onValueChange = onChange,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = Couleurs.NoirEncre,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            visualTransformation = if (motDePasse) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = typeClavier),
            cursorBrush = SolidColor(Couleurs.BleuKlein),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = shape, ambientColor = Color.Black.copy(alpha = 0.15f))
                .background(Color.White, shape)
                .border(2.dp, Couleurs.NoirEncre.copy(alpha = 0.12f), shape)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            decorationBox = { interieur ->
                Box {
                    if (valeur.isEmpty()) {
                        Text(
                            text = placeholder.ifBlank { "…" },
                            color = Couleurs.GrisDoux,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    interieur()
                }
            }
        )
    }
}
