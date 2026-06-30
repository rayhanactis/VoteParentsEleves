package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.model.code
import com.rayhanactis.voteparentseleves.server.auth.PasswordHash
import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.config.tryGetString
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object SeedDemo {
    const val SCRUTIN_ID = "scr-demo-2026"
    const val ADMIN_ID = "admin"
    const val ADMIN_MDP = "admin123"
    const val ELECTEUR_MDP = "0000"

    private data class ListeDemo(
        val id: String,
        val nom: String,
        val slogan: String,
        val description: String,
        val candidats: List<Pair<String, String>>
    )

    private val listesDemo = listOf(
        ListeDemo(
            id = "lst-parents-actifs",
            nom = "Parents Actifs",
            slogan = "Ensemble, faisons grandir l'école",
            description = "Nous voulons une école plus ouverte : davantage d'activités péri-scolaires, un dialogue régulier avec les enseignants et plus de sorties culturelles.",
            candidats = listOf("Alice" to "Dupont", "Hugo" to "Roy", "Léa" to "Nguyen")
        ),
        ListeDemo(
            id = "lst-pour-nos-enfants",
            nom = "Pour Nos Enfants",
            slogan = "Au quotidien, on s'engage",
            description = "Notre priorité, c'est le quotidien des enfants : qualité de la cantine, bâtiments rénovés et sécurité aux abords de l'école.",
            candidats = listOf("Bob" to "Martin", "Sarah" to "Cohen")
        ),
        ListeDemo(
            id = "lst-engagement",
            nom = "Engagement Citoyen",
            slogan = "Une école pour toutes les familles",
            description = "Pour une école qui éduque à la citoyenneté, lutte contre les inégalités et met en avant la diversité de toutes les familles.",
            candidats = listOf("Mehdi" to "Bencheikh", "Camille" to "Dubois",
                "Yui" to "Tanaka", "Reda" to "Mahmoud")
        )
    )

    private val electeursDemo = (1..5).map { i -> "parent$i" to "Parent $i" }

    fun executer() = transaction {
        val dejaPresent = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq SCRUTIN_ID }
            .empty().not()
        if (dejaPresent) return@transaction

        if (AdminsTable.selectAll().where { AdminsTable.id eq ADMIN_ID }.empty()) {
            AdminsTable.insert {
                it[id] = ADMIN_ID
                it[nom] = "Admin Démo"
                it[ecoleId] = "ecole-demo"
                it[motDePasseHash] = PasswordHash.hacher(ADMIN_MDP)
            }
        }

        ScrutinsTable.insert {
            it[id] = SCRUTIN_ID
            it[ecoleId] = "ecole-demo"
            it[nom] = "Élections représentants 2026 (démo)"
            it[dateDebut] = 0
            it[dateFin] = Long.MAX_VALUE
            it[nbSieges] = 8
            it[statut] = StatutScrutin.Ouvert.code()
        }

        listesDemo.forEach { liste ->
            ListesCandidatesTable.insert {
                it[id] = liste.id
                it[nom] = liste.nom
                it[slogan] = liste.slogan
                it[description] = liste.description
                it[scrutinId] = SCRUTIN_ID
            }
            liste.candidats.forEachIndexed { idx, (prenomCand, nomFamille) ->
                CandidatsTable.insert {
                    it[id] = "${liste.id}-c$idx"
                    it[nom] = nomFamille
                    it[prenom] = prenomCand
                    it[CandidatsTable.listeId] = liste.id
                }
            }
        }

        electeursDemo.forEach { (electeurId, nomComplet) ->
            ElecteursTable.insert {
                it[id] = electeurId
                it[nom] = nomComplet
                it[prenom] = "Démo"
                it[ecoleId] = "ecole-demo"
                it[motDePasseHash] = PasswordHash.hacher(ELECTEUR_MDP)
            }
        }
    }
}

fun Application.appliquerSeedSiDemande() {
    val actif = environment.config.tryGetString("dev.seed")?.toBoolean() ?: false
    if (actif) {
        SeedDemo.executer()
        log.info("Seed démo appliqué (ou déjà présent). Scrutin: ${SeedDemo.SCRUTIN_ID}")
        log.info("  Admin: ${SeedDemo.ADMIN_ID} / ${SeedDemo.ADMIN_MDP}")
        log.info("  Électeurs: parent1..parent5 / ${SeedDemo.ELECTEUR_MDP}")
    }
}

