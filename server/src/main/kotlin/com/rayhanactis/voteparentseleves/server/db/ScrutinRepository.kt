package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.model.Bulletin
import com.rayhanactis.voteparentseleves.model.ParticipationScrutin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ScrutinRepository {

    fun nbSieges(scrutinId: String): Int? = transaction {
        ScrutinsTable
            .selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.nbSieges] }
            .singleOrNull()
    }

    fun participation(scrutinId: String): ParticipationScrutin? = transaction {
        val ecoleId = ScrutinsTable
            .selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.ecoleId] }
            .singleOrNull()
            ?: return@transaction null

        val total = ElecteursTable
            .selectAll()
            .where { ElecteursTable.ecoleId eq ecoleId }
            .count()
            .toInt()

        val dates = EmargementsTable
            .selectAll()
            .where { EmargementsTable.scrutinId eq scrutinId }
            .map { it[EmargementsTable.dateVote] }

        ParticipationScrutin(
            scrutinId = scrutinId,
            totalElecteurs = total,
            nbVotants = dates.size,
            dernierVote = dates.filter { it > 0L }.maxOrNull()
        )
    }

    fun bulletins(scrutinId: String): List<Bulletin> = transaction {
        BulletinsTable
            .selectAll()
            .where { BulletinsTable.scrutinId eq scrutinId }
            .map {
                Bulletin(
                    id = it[BulletinsTable.id],
                    scrutinId = it[BulletinsTable.scrutinId],
                    listeCandidateId = it[BulletinsTable.listeCandidateId]
                )
            }
    }
}
