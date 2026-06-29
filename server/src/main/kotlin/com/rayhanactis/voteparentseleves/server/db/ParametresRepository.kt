package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.admin.MiseAJourParametres
import com.rayhanactis.voteparentseleves.model.ParametresEcole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object ParametresRepository {

    private const val ID_SINGLETON = 1

    fun lire(): ParametresEcole = transaction {
        val row = ParametresEcoleTable.selectAll()
            .where { ParametresEcoleTable.id eq ID_SINGLETON }
            .singleOrNull()
        if (row == null) {
            // Initialisation automatique au 1er appel
            ParametresEcoleTable.insert {
                it[id] = ID_SINGLETON
                it[nomEcole] = ""
                it[codePostal] = ""
                it[codeEcole] = "ecole-default"
            }
            ParametresEcole("", "", "ecole-default")
        } else {
            ParametresEcole(
                nomEcole = row[ParametresEcoleTable.nomEcole],
                codePostal = row[ParametresEcoleTable.codePostal],
                codeEcole = row[ParametresEcoleTable.codeEcole],
                emailExpediteur = row[ParametresEcoleTable.emailExpediteur],
                smtpHost = row[ParametresEcoleTable.smtpHost],
                smtpPort = row[ParametresEcoleTable.smtpPort],
                smtpMotDePasse = row[ParametresEcoleTable.smtpMotDePasse]
            )
        }
    }

    fun enregistrer(maj: MiseAJourParametres): ParametresEcole = transaction {
        // S'assure d'avoir une ligne (idempotent)
        lire()
        ParametresEcoleTable.update({ ParametresEcoleTable.id eq ID_SINGLETON }) {
            it[nomEcole] = maj.nomEcole.trim()
            it[codePostal] = maj.codePostal.trim()
            it[codeEcole] = maj.codeEcole.trim().ifBlank { "ecole-default" }
            it[emailExpediteur] = maj.emailExpediteur.trim()
            it[smtpHost] = maj.smtpHost.trim()
            it[smtpPort] = maj.smtpPort
            it[smtpMotDePasse] = maj.smtpMotDePasse
        }
        lire()
    }

    /** Raccourci utilisé par les autres repos pour remplir automatiquement `ecoleId`. */
    fun codeEcoleActuel(): String = lire().codeEcole
}
