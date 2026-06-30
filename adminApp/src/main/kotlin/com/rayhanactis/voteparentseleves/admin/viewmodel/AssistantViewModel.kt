package com.rayhanactis.voteparentseleves.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhanactis.voteparentseleves.admin.ia.AssistantIA
import com.rayhanactis.voteparentseleves.admin.ia.FabriqueAssistant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RoleMessage { UTILISATEUR, ASSISTANT, ERREUR }

data class MessageAssistant(val role: RoleMessage, val texte: String)

class AssistantViewModel : ViewModel() {

    val messages = mutableStateListOf(
        MessageAssistant(
            RoleMessage.ASSISTANT,
            "Bonjour ! Je suis l'assistant d'aide. Posez-moi une question sur l'utilisation de " +
                "l'application : créer un scrutin, importer les parents, imprimer les QR codes, " +
                "programmer une ouverture, dépouiller, exporter les résultats…"
        )
    )

    var saisie by mutableStateOf("")
        private set

    var modele by mutableStateOf(FabriqueAssistant.MODELE_PAR_DEFAUT)
        private set

    var enCours by mutableStateOf(false)
        private set

    // Recréé paresseusement au premier envoi (ou après changement de modèle).
    private var assistant: AssistantIA? = null

    fun majSaisie(valeur: String) {
        saisie = valeur
    }

    fun majModele(valeur: String) {
        modele = valeur
        assistant = null
    }

    fun envoyer() {
        val question = saisie.trim()
        if (question.isEmpty() || enCours) return
        messages.add(MessageAssistant(RoleMessage.UTILISATEUR, question))
        saisie = ""
        enCours = true
        viewModelScope.launch {
            val resultat = withContext(Dispatchers.IO) {
                runCatching {
                    val a = assistant ?: FabriqueAssistant.creer(modele).also { assistant = it }
                    a.repondre(question)
                }
            }
            enCours = false
            resultat.fold(
                onSuccess = { messages.add(MessageAssistant(RoleMessage.ASSISTANT, it.trim())) },
                onFailure = { messages.add(MessageAssistant(RoleMessage.ERREUR, messageErreur(it))) }
            )
        }
    }

    private fun messageErreur(t: Throwable): String =
        "Impossible de contacter le modèle. Vérifiez qu'Ollama est lancé (commande « ollama serve ») " +
            "et que le modèle « $modele » est installé (« ollama pull $modele »).\n\n" +
            "Détail technique : ${t.message ?: t::class.simpleName}"
}
