package com.rayhanactis.voteparentseleves.admin.ui.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Quatre listes déroulantes côte à côte pour saisir année / mois / jour / heure.
 * Plus accessible qu'un champ texte JJ/MM/AAAA pour les secrétaires.
 * Chaque sous-champ a son propre label clair.
 *
 * - Le nombre de jours s'adapte au mois sélectionné (gestion 28/29/30/31)
 * - Plage d'années : année courante → +5 ans
 * - Heures : 0-23, minute fixée à 0
 */
@Composable
fun SelecteurDateHeure(
    label: String,
    valeur: LocalDateTime,
    onChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val anneeActuelle = LocalDateTime.now().year
    val annees = (anneeActuelle..anneeActuelle + 5).toList()
    val mois = (1..12).toList()
    val joursMax = YearMonth.of(valeur.year, valeur.monthValue).lengthOfMonth()
    val jours = (1..joursMax).toList()
    val heures = (0..23).toList()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Couleurs.NoirEncre
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1.3f)) {
                ChampDeroulant(
                    sousLabel = "Année",
                    valeur = valeur.year,
                    choix = annees,
                    onChange = { a ->
                        val jMax = YearMonth.of(a, valeur.monthValue).lengthOfMonth()
                        onChange(valeur.withYear(a).withDayOfMonth(valeur.dayOfMonth.coerceAtMost(jMax)))
                    },
                    formatte = { it.toString() }
                )
            }
            Box(modifier = Modifier.weight(1.4f)) {
                ChampDeroulant(
                    sousLabel = "Mois",
                    valeur = valeur.monthValue,
                    choix = mois,
                    onChange = { m ->
                        val jMax = YearMonth.of(valeur.year, m).lengthOfMonth()
                        onChange(valeur.withMonth(m).withDayOfMonth(valeur.dayOfMonth.coerceAtMost(jMax)))
                    },
                    formatte = { libelleMois(it) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ChampDeroulant(
                    sousLabel = "Jour",
                    valeur = valeur.dayOfMonth,
                    choix = jours,
                    onChange = { j -> onChange(valeur.withDayOfMonth(j)) },
                    formatte = { it.toString().padStart(2, '0') }
                )
            }
            Box(modifier = Modifier.weight(1.1f)) {
                ChampDeroulant(
                    sousLabel = "Heure",
                    valeur = valeur.hour,
                    choix = heures,
                    onChange = { h -> onChange(valeur.withHour(h).withMinute(0)) },
                    formatte = { "${it.toString().padStart(2, '0')} h" }
                )
            }
        }
    }
}

@Composable
private fun ChampDeroulant(
    sousLabel: String,
    valeur: Int,
    choix: List<Int>,
    onChange: (Int) -> Unit,
    formatte: (Int) -> String
) {
    var ouvert by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = sousLabel,
            style = MaterialTheme.typography.labelMedium,
            color = Couleurs.GrisDoux
        )
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape)
                    .border(1.5.dp, Couleurs.NoirEncre.copy(alpha = 0.15f), shape)
                    .clickable { ouvert = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = formatte(valeur),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.NoirEncre,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "▾",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux
                    )
                }
            }
            DropdownMenu(
                expanded = ouvert,
                onDismissRequest = { ouvert = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                choix.forEach { c ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = formatte(c),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (c == valeur) Couleurs.BleuKlein else Couleurs.NoirEncre
                            )
                        },
                        onClick = {
                            onChange(c)
                            ouvert = false
                        }
                    )
                }
            }
        }
    }
}

private fun libelleMois(n: Int): String = when (n) {
    1 -> "janvier"; 2 -> "février"; 3 -> "mars"; 4 -> "avril"
    5 -> "mai"; 6 -> "juin"; 7 -> "juillet"; 8 -> "août"
    9 -> "septembre"; 10 -> "octobre"; 11 -> "novembre"; 12 -> "décembre"
    else -> "?"
}
