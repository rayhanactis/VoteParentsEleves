package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.rayhanactis.voteparentseleves.admin.ui.composants.ColonneDefilante
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.CreationCandidat
import com.rayhanactis.voteparentseleves.admin.viewmodel.CreationListeViewModel
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatCreationListe
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.Electeur
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs

@Composable
fun EcranCreationListe(
    token: String,
    scrutinId: String,
    listeExistante: ListeCandidate? = null,
    onAnnuler: () -> Unit,
    onCree: (ListeCandidate) -> Unit
) {
    val api = LocalApiClient.current
    val vm: CreationListeViewModel = viewModel { CreationListeViewModel(api, token) }

    var nomListe by remember { mutableStateOf(listeExistante?.nom ?: "") }
    var slogan by remember { mutableStateOf(listeExistante?.slogan ?: "") }
    var description by remember { mutableStateOf(listeExistante?.description ?: "") }
    val candidats = remember {
        mutableStateListOf<CreationCandidat>().apply {
            listeExistante?.candidats?.forEach {
                add(CreationCandidat(nom = it.nom, prenom = it.prenom))
            }
        }
    }

    var parents by remember { mutableStateOf<List<Electeur>>(emptyList()) }
    var erreurRepertoire by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        when (val r = api.listerParents(token)) {
            is ApiResult.Succes -> parents = r.data
            is ApiResult.Echec -> erreurRepertoire = r.messageLisible()
            is ApiResult.Reseau -> erreurRepertoire = r.messageLisible()
        }
    }

    val etat = vm.etat
    val envoi = etat is EtatCreationListe.Envoi
    val modeEdition = listeExistante != null

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        ColonneDefilante(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 720.dp)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (modeEdition) "Modifier la liste" else "Nouvelle liste candidate",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Couleurs.NoirEncre
                    )

                    ChampTexte(
                        valeur = nomListe,
                        onChange = { nomListe = it; vm.resetErreur() },
                        label = "Nom de la liste",
                        placeholder = "ex : Parents Actifs"
                    )

                    ChampTexte(
                        valeur = slogan,
                        onChange = { slogan = it },
                        label = "Slogan (optionnel)",
                        placeholder = "ex : Ensemble, faisons grandir l'école"
                    )

                    ChampTexte(
                        valeur = description,
                        onChange = { description = it },
                        label = "Description / profession de foi (optionnel)",
                        placeholder = "Présentez en quelques phrases les engagements de la liste."
                    )

                    SelecteurCandidats(
                        parents = parents,
                        erreur = erreurRepertoire,
                        dejaSelectionnesIds = candidats.mapNotNull { it.electeurId }.toSet(),
                        onAjouterParent = { p ->
                            candidats.add(
                                CreationCandidat(
                                    nom = p.nom,
                                    prenom = p.prenom,
                                    electeurId = p.id
                                )
                            )
                            vm.resetErreur()
                        }
                    )

                    if (candidats.isEmpty()) {
                        AucunCandidat()
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            candidats.forEachIndexed { index, candidat ->
                                LigneCandidat(
                                    index = index + 1,
                                    candidat = candidat,
                                    onSupprimer = { candidats.removeAt(index) }
                                )
                            }
                        }
                    }

                    if (etat is EtatCreationListe.Erreur) MessageErreur(etat.message)

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BoutonClay(
                            texte = "Annuler",
                            onClick = onAnnuler,
                            modifier = Modifier.weight(1f),
                            couleur = Couleurs.GrisDoux,
                            enabled = !envoi
                        )
                        BoutonClay(
                            texte = if (envoi) "ENREGISTREMENT…"
                                else if (modeEdition) "ENREGISTRER LES MODIFICATIONS"
                                else "ENREGISTRER LA LISTE",
                            onClick = {
                                vm.enregistrer(
                                    scrutinId = scrutinId,
                                    listeIdExistante = listeExistante?.id,
                                    nom = nomListe,
                                    slogan = slogan,
                                    description = description,
                                    candidats = candidats.toList(),
                                    onSucces = onCree
                                )
                            },
                            modifier = Modifier.weight(1.6f),
                            couleur = Couleurs.BleuKlein,
                            enabled = !envoi
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelecteurCandidats(
    parents: List<Electeur>,
    erreur: String?,
    dejaSelectionnesIds: Set<String>,
    onAjouterParent: (Electeur) -> Unit
) {
    var recherche by remember { mutableStateOf("") }
    var menuOuvert by remember { mutableStateOf(false) }

    val suggestions = remember(parents, recherche, dejaSelectionnesIds) {
        val q = recherche.trim().lowercase()
        parents
            .filter { p -> p.id !in dejaSelectionnesIds }
            .filter { p ->
                q.isBlank() ||
                    p.nom.lowercase().contains(q) ||
                    p.prenom.lowercase().contains(q)
            }
            .take(10)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Candidat·es (sélection depuis le répertoire des parents)",
            style = MaterialTheme.typography.titleMedium,
            color = Couleurs.NoirEncre
        )
        if (erreur != null) {
            Text(
                text = "Répertoire indisponible : $erreur",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.RougePompidou
            )
        }
        if (parents.isEmpty() && erreur == null) {
            Text(
                text = "Le répertoire des parents est vide. Ajoutez d'abord des parents " +
                    "via l'onglet « Parents d'élèves » avant de constituer une liste.",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )
        } else {
            Box {
                ChampTexte(
                    valeur = recherche,
                    onChange = {
                        recherche = it
                        menuOuvert = it.isNotEmpty()
                    },
                    label = "Rechercher un parent à ajouter",
                    placeholder = "Tapez un nom ou un prénom…"
                )
                DropdownMenu(
                    expanded = menuOuvert && suggestions.isNotEmpty(),
                    onDismissRequest = { menuOuvert = false }
                ) {
                    suggestions.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${p.prenom} ${p.nom}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Couleurs.NoirEncre
                                    )
                                    Text(
                                        text = p.id,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Couleurs.GrisDoux
                                    )
                                }
                            },
                            onClick = {
                                onAjouterParent(p)
                                recherche = ""
                                menuOuvert = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneCandidat(
    index: Int,
    candidat: CreationCandidat,
    onSupprimer: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape)
            .border(1.dp, Couleurs.NoirEncre.copy(alpha = 0.08f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Couleurs.BleuKlein, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${candidat.prenom} ${candidat.nom}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Couleurs.NoirEncre
                )
                if (candidat.electeurId != null) {
                    Text(
                        text = "Lié au parent ${candidat.electeurId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Couleurs.GrisDoux
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSupprimer() }
                    .padding(8.dp)
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.titleMedium,
                    color = Couleurs.RougePompidou
                )
            }
        }
    }
}

@Composable
private fun AucunCandidat() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Couleurs.NoirEncre.copy(alpha = 0.04f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aucun candidat ajouté pour l'instant.",
            style = MaterialTheme.typography.bodyMedium,
            color = Couleurs.GrisDoux
        )
    }
}

@Composable
private fun MessageErreur(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Couleurs.RougePompidou.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Couleurs.RougePompidou
        )
    }
}
