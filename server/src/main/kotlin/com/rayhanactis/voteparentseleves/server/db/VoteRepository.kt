package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.model.code
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

sealed class ResultatDepotVote {
    data class Succes(val bulletinId: String) : ResultatDepotVote()
    data object ScrutinInconnu : ResultatDepotVote()
    data object ScrutinNonOuvert : ResultatDepotVote()
    data object ElecteurInconnu : ResultatDepotVote()
    data object DejaVote : ResultatDepotVote()
    data object ListeInvalide : ResultatDepotVote()
}

object VoteRepository {

    fun aVote(scrutinId: String, electeurId: String): Boolean = transaction {
        EmargementsTable
            .selectAll()
            .where {
                (EmargementsTable.electeurId eq electeurId) and
                    (EmargementsTable.scrutinId eq scrutinId)
            }
            .empty()
            .not()
    }

    fun deposer(
        scrutinId: String,
        electeurId: String,
        listeCandidateId: String?
    ): ResultatDepotVote = transaction {
        val statut = ScrutinsTable
            .selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.statut] }
            .singleOrNull()
            ?: return@transaction ResultatDepotVote.ScrutinInconnu

        if (statut != StatutScrutin.Ouvert.code()) {
            return@transaction ResultatDepotVote.ScrutinNonOuvert
        }

        val electeurExiste = ElecteursTable
            .selectAll()
            .where { ElecteursTable.id eq electeurId }
            .empty()
            .not()
        if (!electeurExiste) return@transaction ResultatDepotVote.ElecteurInconnu

        val dejaEmarge = EmargementsTable
            .selectAll()
            .where {
                (EmargementsTable.electeurId eq electeurId) and
                    (EmargementsTable.scrutinId eq scrutinId)
            }
            .empty()
            .not()
        if (dejaEmarge) return@transaction ResultatDepotVote.DejaVote

        if (listeCandidateId != null) {
            val listeValide = ListesCandidatesTable
                .selectAll()
                .where {
                    (ListesCandidatesTable.id eq listeCandidateId) and
                        (ListesCandidatesTable.scrutinId eq scrutinId)
                }
                .empty()
                .not()
            if (!listeValide) return@transaction ResultatDepotVote.ListeInvalide
        }

        val bulletinId = "blt-${UUID.randomUUID()}"
        BulletinsTable.insert {
            it[id] = bulletinId
            it[BulletinsTable.scrutinId] = scrutinId
            it[BulletinsTable.listeCandidateId] = listeCandidateId
        }
        EmargementsTable.insert {
            it[EmargementsTable.electeurId] = electeurId
            it[EmargementsTable.scrutinId] = scrutinId
            it[EmargementsTable.dateVote] = System.currentTimeMillis()
        }

        ResultatDepotVote.Succes(bulletinId)
    }
}
