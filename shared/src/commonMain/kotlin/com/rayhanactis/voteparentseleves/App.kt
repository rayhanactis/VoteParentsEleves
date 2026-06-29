package com.rayhanactis.voteparentseleves

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.rayhanactis.voteparentseleves.api.ApiClient
import com.rayhanactis.voteparentseleves.api.ApiResult
import com.rayhanactis.voteparentseleves.api.LocalApiClient
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.ui.composants.BoutonClay
import com.rayhanactis.voteparentseleves.ui.composants.CartePleine
import com.rayhanactis.voteparentseleves.ui.composants.FondKandinsky
import com.rayhanactis.voteparentseleves.ui.theme.Couleurs
import com.rayhanactis.voteparentseleves.ui.ecrans.ChoixVote
import com.rayhanactis.voteparentseleves.ui.ecrans.Ecran
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranChoixScrutin
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranConfirmation
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranDecouverte
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranListes
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranLogin
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranRecu
import com.rayhanactis.voteparentseleves.ui.ecrans.EcranVote
import com.rayhanactis.voteparentseleves.ui.ecrans.EtapeAcces
import com.rayhanactis.voteparentseleves.ui.theme.AppTheme

private const val BASE_URL_DEMO = "http://localhost:8080"

@Composable
@Preview
fun App(baseUrlDemo: String = BASE_URL_DEMO) {
    var etapeAcces by remember { mutableStateOf<EtapeAcces>(EtapeAcces.Decouverte) }

    when (val etape = etapeAcces) {
        EtapeAcces.Decouverte -> AppTheme {
            EcranDecouverte(
                baseUrlDemo = baseUrlDemo,
                onBaseUrlChoisie = { baseUrl -> etapeAcces = EtapeAcces.Chargement(baseUrl) }
            )
        }

        is EtapeAcces.Chargement -> {
            val apiClient = remember(etape.baseUrl) { ApiClient(etape.baseUrl) }
            LaunchedEffect(etape.baseUrl) {
                when (val r = apiClient.listerScrutins()) {
                    is ApiResult.Succes -> {
                        val ouverts = r.data.filter { it.statut == StatutScrutin.Ouvert }
                        etapeAcces = when {
                            ouverts.size == 1 -> EtapeAcces.PretAVoter(etape.baseUrl, ouverts.single().id)
                            ouverts.isEmpty() -> EtapeAcces.SalleAttente(etape.baseUrl)
                            else -> EtapeAcces.ChoixScrutin(etape.baseUrl, ouverts)
                        }
                    }
                    is ApiResult.Echec -> etapeAcces = EtapeAcces.Erreur(etape.baseUrl, r.messageLisible())
                    is ApiResult.Reseau -> etapeAcces = EtapeAcces.Erreur(etape.baseUrl, r.messageLisible())
                }
            }
            AppTheme { EcranChargement() }
        }

        is EtapeAcces.ChoixScrutin -> AppTheme {
            EcranChoixScrutin(
                scrutins = etape.scrutins,
                onChoisir = { scrutin -> etapeAcces = EtapeAcces.PretAVoter(etape.baseUrl, scrutin.id) }
            )
        }

        is EtapeAcces.SalleAttente -> AppTheme {
            EcranSalleAttente(
                baseUrl = etape.baseUrl,
                onScrutinOuvert = { scrutinId ->
                    etapeAcces = EtapeAcces.PretAVoter(etape.baseUrl, scrutinId)
                },
                onRetour = { etapeAcces = EtapeAcces.Decouverte }
            )
        }

        is EtapeAcces.Erreur -> AppTheme {
            EcranErreurAcces(
                message = etape.message,
                onReessayer = { etapeAcces = EtapeAcces.Decouverte }
            )
        }

        is EtapeAcces.PretAVoter -> {
            val apiClient = remember(etape.baseUrl) { ApiClient(etape.baseUrl) }
            CompositionLocalProvider(LocalApiClient provides apiClient) {
                AppTheme {
                    NavigationVote(
                        scrutinId = etape.scrutinId,
                        onQuitter = { etapeAcces = EtapeAcces.Decouverte }
                    )
                }
            }
        }
    }
}

@Composable
private fun EcranChargement() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EcranErreurAcces(message: String, onReessayer: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 420.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Embleme()
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "On ne peut pas voter pour l'instant",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = Couleurs.NoirEncre,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = message,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    BoutonClay(
                        texte = "Réessayer",
                        onClick = onReessayer,
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.BleuKlein
                    )
                }
            }
        }
    }
}

// Salle d'attente : le client est connecté au serveur de l'école mais aucun
// scrutin n'est encore ouvert. On interroge le serveur toutes les 3 s ; dès
// qu'un scrutin passe « ouvert », on bascule en « connexion en cours » puis on
// entre dans le parcours de vote — ainsi tout le monde entre en même temps.
@Composable
private fun EcranSalleAttente(
    baseUrl: String,
    onScrutinOuvert: (scrutinId: String) -> Unit,
    onRetour: () -> Unit
) {
    val apiClient = remember(baseUrl) { ApiClient(baseUrl) }
    var ouvert by remember { mutableStateOf(false) }

    LaunchedEffect(baseUrl) {
        while (true) {
            val r = apiClient.listerScrutins()
            if (r is ApiResult.Succes) {
                val scrutinOuvert = r.data.firstOrNull { it.statut == StatutScrutin.Ouvert }
                if (scrutinOuvert != null) {
                    ouvert = true
                    delay(1200) // laisse voir « Scrutin ouvert, connexion en cours… »
                    onScrutinOuvert(scrutinOuvert.id)
                    return@LaunchedEffect
                }
            }
            delay(3000)
        }
    }

    val accent = if (ouvert) Couleurs.VertMenthe else Couleurs.BleuKlein

    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 440.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmblemePulse(accent = accent)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = if (ouvert) "Scrutin ouvert" else "Salle d'attente",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Couleurs.NoirEncre,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (ouvert) {
                            "Scrutin ouvert, connexion en cours…"
                        } else {
                            "Veuillez patienter, le scrutin sera bientôt ouvert…"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux,
                        textAlign = TextAlign.Center
                    )
                    if (!ouvert) {
                        Spacer(Modifier.height(28.dp))
                        BoutonClay(
                            texte = "Retour",
                            onClick = onRetour,
                            modifier = Modifier.fillMaxWidth(),
                            couleur = Couleurs.GrisDoux
                        )
                    }
                }
            }
        }
    }
}

// Emblème « vivant » : un halo qui pulse autour d'un cœur fixe.
@Composable
private fun EmblemePulse(accent: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "scale"
    )
    val alphaHalo by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(modifier = Modifier.size(116.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .scale(scale)
                .alpha(alphaHalo)
                .background(accent, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(accent, CircleShape)
        )
    }
}

// Petit emblème décoratif (cercles concentriques aux couleurs de l'app)
// pour adoucir l'écran d'attente / d'erreur.
@Composable
private fun Embleme() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(Couleurs.BleuKlein.copy(alpha = 0.10f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Couleurs.BleuKlein.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Couleurs.BleuKlein, CircleShape)
            )
        }
    }
}

// Écran de remerciement affiché quand l'électeur a déjà voté pour ce scrutin.
@Composable
private fun EcranDejaVote(onRetourAccueil: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        FondKandinsky()
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CartePleine(modifier = Modifier.widthIn(max = 440.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Couleurs.VertMenthe.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Couleurs.VertMenthe, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Couleurs.BlancCasse
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Merci pour votre vote",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Couleurs.NoirEncre,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Votre voix a bien été enregistrée. À bientôt !",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Couleurs.GrisDoux,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    BoutonClay(
                        texte = "Revenir à l'accueil",
                        onClick = onRetourAccueil,
                        modifier = Modifier.fillMaxWidth(),
                        couleur = Couleurs.BleuKlein
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationVote(scrutinId: String, onQuitter: () -> Unit) {
    var ecran by remember { mutableStateOf<Ecran>(Ecran.Login) }

    when (val e = ecran) {
        Ecran.Login -> EcranLogin(
            scrutinId = scrutinId,
            onConnecte = { token, scrutinIdConnecte, dejaVote ->
                ecran = if (dejaVote) {
                    Ecran.DejaVote
                } else {
                    Ecran.ListesPresentation(token = token, scrutinId = scrutinIdConnecte)
                }
            }
        )

        Ecran.DejaVote -> EcranDejaVote(onRetourAccueil = onQuitter)

        is Ecran.ListesPresentation -> EcranListes(
            scrutinId = e.scrutinId,
            onContinuerVersVote = { listes ->
                ecran = Ecran.Vote(token = e.token, scrutinId = e.scrutinId, listes = listes)
            }
        )

        is Ecran.Vote -> EcranVote(
            listes = e.listes,
            onValider = { choix ->
                ecran = Ecran.Confirmation(
                    token = e.token,
                    scrutinId = e.scrutinId,
                    listes = e.listes,
                    choix = choix
                )
            },
            onRevoirListes = {
                ecran = Ecran.ListesPresentation(token = e.token, scrutinId = e.scrutinId)
            }
        )

        is Ecran.Confirmation -> EcranConfirmation(
            token = e.token,
            scrutinId = e.scrutinId,
            choix = e.choix,
            onModifier = {
                ecran = Ecran.Vote(token = e.token, scrutinId = e.scrutinId, listes = e.listes)
            },
            onVoteEnregistre = { recu ->
                val nom = (e.choix as? ChoixVote.PourListe)?.nomListe
                ecran = Ecran.Recu(bulletinId = recu.bulletinId, nomListe = nom)
            }
        )

        is Ecran.Recu -> EcranRecu(
            bulletinId = e.bulletinId,
            nomListe = e.nomListe,
            onRetour = { ecran = Ecran.Login }
        )
    }
}
