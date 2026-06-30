package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.admin.CreationParent
import com.rayhanactis.voteparentseleves.admin.IdentifiantsGeneres
import com.rayhanactis.voteparentseleves.admin.ModificationParent
import com.rayhanactis.voteparentseleves.model.Electeur
import com.rayhanactis.voteparentseleves.server.auth.PasswordHash
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

sealed class ResultatActionParent {
    data class Succes(val parent: Electeur) : ResultatActionParent()
    data object Introuvable : ResultatActionParent()
    data class Bloque(val raison: String) : ResultatActionParent()
    data class DonneesInvalides(val raison: String) : ResultatActionParent()
}

object RepertoireRepository {

    fun listerParents(): List<Electeur> = transaction {
        ElecteursTable.selectAll().map(::rowToElecteur)
    }

    fun lireParent(electeurId: String): Electeur? = transaction {
        ElecteursTable.selectAll()
            .where { ElecteursTable.id eq electeurId }
            .map(::rowToElecteur)
            .singleOrNull()
    }

    fun creerParent(input: CreationParent): ResultatActionParent = transaction {
        if (input.nom.isBlank() || input.prenom.isBlank()) {
            return@transaction ResultatActionParent.DonneesInvalides("Nom et prénom obligatoires.")
        }
        val nouvelId = "el-${UUID.randomUUID().toString().take(8)}"
        val ecoleId = ParametresRepository.codeEcoleActuel()
        ElecteursTable.insert {
            it[id] = nouvelId
            it[nom] = input.nom.trim()
            it[prenom] = input.prenom.trim()
            it[ElecteursTable.ecoleId] = ecoleId
            it[email] = input.email.trim()
        }
        ResultatActionParent.Succes(
            Electeur(nouvelId, input.nom.trim(), input.prenom.trim(), ecoleId, input.email.trim())
        )
    }

    fun modifierParent(electeurId: String, input: ModificationParent): ResultatActionParent = transaction {
        if (input.nom.isBlank() || input.prenom.isBlank()) {
            return@transaction ResultatActionParent.DonneesInvalides("Nom et prénom obligatoires.")
        }
        val nbMaj = ElecteursTable.update({ ElecteursTable.id eq electeurId }) {
            it[nom] = input.nom.trim()
            it[prenom] = input.prenom.trim()
            it[email] = input.email.trim()
        }
        if (nbMaj == 0) return@transaction ResultatActionParent.Introuvable
        val maj = ElecteursTable.selectAll()
            .where { ElecteursTable.id eq electeurId }
            .map(::rowToElecteur)
            .singleOrNull() ?: return@transaction ResultatActionParent.Introuvable
        ResultatActionParent.Succes(maj)
    }

    fun genererMotDePasse(electeurId: String): IdentifiantsGeneres? = transaction {
        val existe = ElecteursTable.selectAll()
            .where { ElecteursTable.id eq electeurId }
            .empty().not()
        if (!existe) return@transaction null
        val motDePasse = genererMotDePasseLisible()
        ElecteursTable.update({ ElecteursTable.id eq electeurId }) {
            it[motDePasseHash] = PasswordHash.hacher(motDePasse)
        }
        IdentifiantsGeneres(identifiant = electeurId, motDePasseClair = motDePasse)
    }

    fun supprimerParent(electeurId: String): SuppressionResultat = transaction {
        val existe = ElecteursTable.selectAll()
            .where { ElecteursTable.id eq electeurId }
            .empty().not()
        if (!existe) return@transaction SuppressionResultat.Introuvable

        val aDejaVote = EmargementsTable.selectAll()
            .where { EmargementsTable.electeurId eq electeurId }
            .empty().not()
        if (aDejaVote) {
            return@transaction SuppressionResultat.Bloquee(
                "Ce parent a déjà voté dans au moins un scrutin. Suppression refusée."
            )
        }
        val estCandidat = CandidatsTable.selectAll()
            .where { CandidatsTable.electeurId eq electeurId }
            .empty().not()
        if (estCandidat) {
            return@transaction SuppressionResultat.Bloquee(
                "Ce parent est candidat sur une liste. Retirez-le d'abord de la liste."
            )
        }

        ElecteursTable.deleteWhere { ElecteursTable.id eq electeurId }
        SuppressionResultat.Succes
    }

    private fun rowToElecteur(row: org.jetbrains.exposed.sql.ResultRow): Electeur = Electeur(
        id = row[ElecteursTable.id],
        nom = row[ElecteursTable.nom],
        prenom = row[ElecteursTable.prenom],
        ecoleId = row[ElecteursTable.ecoleId],
        email = row[ElecteursTable.email],
        aVote = row[ElecteursTable.aVote]
    )
}
