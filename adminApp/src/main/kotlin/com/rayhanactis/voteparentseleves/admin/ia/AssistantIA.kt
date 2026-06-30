package com.rayhanactis.voteparentseleves.admin.ia

import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.AiServices
import java.time.Duration

/**
 * Assistant d'aide à la navigation dans l'application admin (bonus jury, branche
 * feature/ia). RAG « pauvre » : le manuel est injecté en message système et le
 * modèle répond UNIQUEMENT dessus. Strictement consultatif — il explique, il ne
 * déclenche jamais d'action sur une élection.
 */
interface AssistantIA {
    fun repondre(question: String): String
}

object FabriqueAssistant {

    const val MODELE_PAR_DEFAUT = "llama3.2"
    const val URL_OLLAMA = "http://localhost:11434"

    private val INSTRUCTIONS = """
        Tu es l'assistant d'aide de l'application « VoteParentsEleves », un outil destiné aux
        secrétaires et directions d'école pour organiser les élections de représentants de
        parents d'élèves.

        Ton rôle : expliquer comment utiliser l'application, en t'appuyant UNIQUEMENT sur le
        manuel ci-dessous. Tu réponds en français, simplement, à une personne non technique.

        Règles strictes :
        - Tu ne fais qu'EXPLIQUER. Tu ne déclenches JAMAIS d'action : tu ne peux pas ouvrir,
          fermer ni dépouiller un scrutin, ni modifier de données. Indique plutôt l'écran et
          le bouton à utiliser.
        - Si la réponse n'est pas dans le manuel, dis-le honnêtement et invite à contacter le
          référent de l'établissement. N'invente rien, surtout sur la sécurité, l'anonymat du
          vote ou les résultats.
        - Reste bref : quelques phrases, et oriente vers le bon écran ou bouton.

        MANUEL :
        $MANUEL
    """.trimIndent()

    /**
     * Construit l'assistant. Ne contacte pas Ollama ici (la connexion est paresseuse, au
     * premier appel) ; lève une exception au premier `repondre` si Ollama est injoignable.
     */
    fun creer(
        modele: String = MODELE_PAR_DEFAUT,
        baseUrl: String = URL_OLLAMA
    ): AssistantIA {
        val chatModel = OllamaChatModel.builder()
            .baseUrl(baseUrl)
            .modelName(modele)
            .temperature(0.2)
            .timeout(Duration.ofSeconds(180))
            .build()

        return AiServices.builder(AssistantIA::class.java)
            .chatModel(chatModel)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
            .systemMessageProvider { INSTRUCTIONS }
            .build()
    }
}

private const val MANUEL = """
1. CONNEXION
   - Lancez l'application admin. Connectez-vous avec votre identifiant et mot de passe.
   - En démonstration : identifiant « admin », mot de passe « admin123 ».

2. CRÉER UN SCRUTIN
   - Depuis le tableau de bord, bouton « + Nouveau scrutin ».
   - Renseignez le nom, la date de début, la date de fin et le nombre de sièges à pourvoir.
   - Le scrutin est créé en mode « En configuration » (brouillon).

3. LISTES CANDIDATES
   - Ouvrez le scrutin, puis « + Ajouter une liste ».
   - Donnez un nom à la liste et ajoutez les candidats (nom, prénom).
   - On peut modifier ou supprimer une liste tant que le scrutin est en configuration.

4. ÉLECTEURS (PARENTS)
   - Gérés depuis l'écran « Répertoire » (commun à toute l'école).
   - Import par fichier CSV, ou génération automatique des identifiants et mots de passe.
   - Vous pouvez imprimer une fiche PDF par parent (avec QR code) à découper et distribuer.

5. JOUR DU VOTE
   - Écran « Projeter le QR » : affiche un QR code que les téléphones des parents scannent
     pour trouver le serveur de l'école sur le réseau Wi-Fi local.
   - Chaque parent se connecte avec son code et son mot de passe, puis vote.

6. OUVRIR / PROGRAMMER
   - Bouton « Ouvrir maintenant » : ouvre immédiatement le scrutin au vote.
   - Bouton « Programmer l'ouverture » : le scrutin s'ouvrira et se fermera automatiquement
     aux dates indiquées. L'ouverture programmée apparaît sur le tableau de bord.
   - Un seul scrutin peut être ouvert à la fois.

7. PENDANT LE VOTE
   - Le détail du scrutin affiche la participation en direct (nombre de votants, pourcentage).

8. CLÔTURE ET RÉSULTATS
   - « Fermer le scrutin » arrête le vote, puis « Dépouiller » calcule les résultats.
   - La répartition des sièges utilise la méthode de Hare (proportionnelle au plus fort reste).
   - Bouton « Afficher les résultats » : vue détaillée (votants, sièges par liste, statistiques).
   - Bouton « Exporter en PDF » : procès-verbal officiel à imprimer ou afficher.

9. SÉCURITÉ ET ANONYMAT
   - Le vote est anonyme : l'identité du votant et son bulletin sont séparés en base.
   - Un parent ne peut voter qu'une seule fois par scrutin.

10. SAUVEGARDE
   - Écran « Paramètres » : sauvegarde et restauration de la base de données de l'école.
"""
