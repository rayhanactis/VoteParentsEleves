package com.rayhanactis.voteparentseleves.admin.ui.ecrans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rayhanactis.voteparentseleves.admin.fichiers.choisirDestinationFichier
import com.rayhanactis.voteparentseleves.admin.fichiers.choisirFichierAOuvrir
import com.rayhanactis.voteparentseleves.admin.ui.composants.BarreNavigation
import com.rayhanactis.voteparentseleves.admin.ui.composants.OngletAdmin
import com.rayhanactis.voteparentseleves.admin.viewmodel.EtatParametres
import com.rayhanactis.voteparentseleves.admin.viewmodel.ParametresViewModel
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.ParametresEcole
import com.rayhanactis.voteparentseleves.server.db.BackupRepository
import com.rayhanactis.voteparentseleves.server.db.ResultatRestauration
import com.rayhanactis.voteparentseleves.server.db.ResultatSauvegarde
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.ChampTexte
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun EcranParametres(
    token: String,
    onAllerDashboard: () -> Unit,
    onAllerRepertoire: () -> Unit,
    onDeconnexion: () -> Unit
) {
    val api = LocalApiClient.current
    val vm: ParametresViewModel = viewModel { ParametresViewModel(api, token) }
    val coroutineScope = rememberCoroutineScope()
    var messageBackup by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.charger() }

    Box(modifier = Modifier.fillMaxSize().background(Couleurs.FondCreme)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BarreNavigation(
                actif = OngletAdmin.PARAMETRES,
                onAllerDashboard = onAllerDashboard,
                onAllerRepertoire = onAllerRepertoire,
                onAllerParametres = { vm.charger() },
                onActualiser = { vm.charger() },
                onDeconnexion = onDeconnexion
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (val etat = vm.etat) {
                        EtatParametres.Chargement -> CircularProgressIndicator(color = Couleurs.BleuKlein)
                        is EtatParametres.Erreur -> Text(
                            text = etat.message,
                            style = MaterialTheme.typography.titleMedium,
                            color = Couleurs.RougePompidou
                        )
                        is EtatParametres.Pret -> Formulaire(
                            parametres = etat.parametres,
                            messageSauvegarde = vm.messageSauvegarde,
                            onChangerMessage = { vm.effacerMessage() },
                            onEnregistrer = { n, cp, ce, mail, host, port, mdp ->
                                vm.enregistrer(n, cp, ce, mail, host, port, mdp)
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    SectionSauvegarde(
                        message = messageBackup,
                        onSauvegarder = {
                            coroutineScope.launch {
                                messageBackup = withContext(Dispatchers.IO) {
                                    val horodatage = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
                                    val destination = choisirDestinationFichier(
                                        titre = "Sauvegarder la base de données",
                                        nomParDefaut = "sauvegarde-$horodatage.db"
                                    ) ?: return@withContext null
                                    when (val r = BackupRepository.sauvegarder(destination)) {
                                        is ResultatSauvegarde.Succes -> "Sauvegarde créée : ${r.fichier.name}"
                                        is ResultatSauvegarde.Echec -> "Échec de la sauvegarde : ${r.raison}"
                                    }
                                }
                            }
                        },
                        onRestaurer = {
                            coroutineScope.launch {
                                messageBackup = withContext(Dispatchers.IO) {
                                    val source = choisirFichierAOuvrir("Restaurer une sauvegarde", "db") ?: return@withContext null
                                    when (val r = BackupRepository.restaurer(source)) {
                                        ResultatRestauration.Succes -> "Base restaurée depuis ${source.name}."
                                        is ResultatRestauration.Echec -> "Échec de la restauration : ${r.raison}"
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionSauvegarde(
    message: String?,
    onSauvegarder: () -> Unit,
    onRestaurer: () -> Unit
) {
    CartePleine(modifier = Modifier.widthIn(max = 720.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Sauvegarde de la base de données",
                style = MaterialTheme.typography.headlineMedium,
                color = Couleurs.NoirEncre
            )
            Text(
                text = "Exportez un fichier .db unique et consistant (utilisable même pendant un vote en cours), " +
                    "ou restaurez une sauvegarde précédente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )
            if (message != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Couleurs.BleuKlein.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(message, color = Couleurs.BleuKlein, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BoutonClay(
                    texte = "Sauvegarder",
                    onClick = onSauvegarder,
                    modifier = Modifier.weight(1f),
                    couleur = Couleurs.VertMenthe
                )
                BoutonClay(
                    texte = "Restaurer",
                    onClick = onRestaurer,
                    modifier = Modifier.weight(1f),
                    couleur = Couleurs.OrangeCh
                )
            }
            Text(
                text = "La restauration remplace immédiatement les données actuelles. À utiliser avec précaution.",
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.RougePompidou
            )
        }
    }
}

@Composable
private fun Formulaire(
    parametres: ParametresEcole,
    messageSauvegarde: String?,
    onChangerMessage: () -> Unit,
    onEnregistrer: (String, String, String, String, String, Int, String) -> Unit
) {
    val cle = parametres.codeEcole
    var nomEcole by remember(cle) { mutableStateOf(parametres.nomEcole) }
    var codePostal by remember(cle) { mutableStateOf(parametres.codePostal) }
    var codeEcole by remember(cle) { mutableStateOf(parametres.codeEcole) }
    var emailExpediteur by remember(cle) { mutableStateOf(parametres.emailExpediteur) }
    var smtpHost by remember(cle) { mutableStateOf(parametres.smtpHost) }
    var smtpPort by remember(cle) { mutableStateOf(parametres.smtpPort.toString()) }
    var smtpMotDePasse by remember(cle) { mutableStateOf(parametres.smtpMotDePasse) }

    CartePleine(modifier = Modifier.widthIn(max = 720.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Paramètres de l'établissement",
                style = MaterialTheme.typography.headlineLarge,
                color = Couleurs.NoirEncre
            )
            Text(
                text = "Ces informations apparaissent dans les courriers et identifient l'école auprès des parents.",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )

            ChampTexte(
                valeur = nomEcole,
                onChange = { nomEcole = it; onChangerMessage() },
                label = "Nom de l'école",
                placeholder = "ex : École Jean-Moulin"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ChampTexte(
                        valeur = codePostal,
                        onChange = { codePostal = it.filter { c -> c.isDigit() }.take(5); onChangerMessage() },
                        label = "Code postal",
                        placeholder = "75001"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChampTexte(
                        valeur = codeEcole,
                        onChange = { codeEcole = it; onChangerMessage() },
                        label = "Code interne de l'école",
                        placeholder = "ecole-jean-moulin"
                    )
                }
            }

            Text(
                text = "Le code interne est utilisé techniquement pour identifier l'école " +
                    "dans les données (scrutins, parents). Pas affiché aux parents.",
                style = MaterialTheme.typography.labelMedium,
                color = Couleurs.GrisDoux
            )

            Text(
                text = "Envoi des emails (identifiants des parents)",
                style = MaterialTheme.typography.headlineMedium,
                color = Couleurs.NoirEncre
            )
            Text(
                text = "Adresse et serveur d'envoi utilisés pour transmettre aux parents leur identifiant " +
                    "et mot de passe. Pour Gmail : serveur smtp.gmail.com, port 587, et un « mot de passe " +
                    "d'application » (pas votre mot de passe habituel).",
                style = MaterialTheme.typography.bodyMedium,
                color = Couleurs.GrisDoux
            )
            ChampTexte(
                valeur = emailExpediteur,
                onChange = { emailExpediteur = it; onChangerMessage() },
                label = "Email expéditeur",
                placeholder = "ex : elections@ecole-jean-moulin.fr"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.weight(2f)) {
                    ChampTexte(
                        valeur = smtpHost,
                        onChange = { smtpHost = it; onChangerMessage() },
                        label = "Serveur SMTP",
                        placeholder = "smtp.gmail.com"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ChampTexte(
                        valeur = smtpPort,
                        onChange = { smtpPort = it.filter { c -> c.isDigit() }.take(5); onChangerMessage() },
                        label = "Port",
                        placeholder = "587"
                    )
                }
            }
            ChampTexte(
                valeur = smtpMotDePasse,
                onChange = { smtpMotDePasse = it; onChangerMessage() },
                label = "Mot de passe d'application SMTP",
                placeholder = "••••••••",
                motDePasse = true
            )

            if (messageSauvegarde != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Couleurs.VertMenthe.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(messageSauvegarde, color = Couleurs.VertMenthe, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(4.dp))

            BoutonClay(
                texte = "Enregistrer",
                onClick = {
                    onEnregistrer(
                        nomEcole, codePostal, codeEcole,
                        emailExpediteur, smtpHost, smtpPort.toIntOrNull() ?: 587, smtpMotDePasse
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                couleur = Couleurs.BleuKlein,
                enabled = nomEcole.isNotBlank()
            )
        }
    }
}
