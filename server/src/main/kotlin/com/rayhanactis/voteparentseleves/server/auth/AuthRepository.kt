package com.rayhanactis.voteparentseleves.server.auth

import com.rayhanactis.voteparentseleves.auth.AuthContext
import com.rayhanactis.voteparentseleves.auth.AuthResult
import com.rayhanactis.voteparentseleves.auth.AuthenticationProvider
import com.rayhanactis.voteparentseleves.model.RoleUtilisateur
import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object AuthRepository {

    fun verifierElecteur(code: String, motDePasse: String): AuthResult = transaction {
        val row = ElecteursTable.selectAll()
            .where { ElecteursTable.id eq code }
            .singleOrNull()
            ?: return@transaction AuthResult.Failure("Identifiants invalides")
        val hash = row[ElecteursTable.motDePasseHash]
        if (hash.isBlank() || !PasswordHash.verifier(motDePasse, hash)) {
            return@transaction AuthResult.Failure("Identifiants invalides")
        }
        AuthResult.Success(principalId = row[ElecteursTable.id], role = RoleUtilisateur.Electeur)
    }

    fun verifierAdmin(code: String, motDePasse: String): AuthResult = transaction {
        val row = AdminsTable.selectAll()
            .where { AdminsTable.id eq code }
            .singleOrNull()
            ?: return@transaction AuthResult.Failure("Identifiants invalides")
        if (!PasswordHash.verifier(motDePasse, row[AdminsTable.motDePasseHash])) {
            return@transaction AuthResult.Failure("Identifiants invalides")
        }
        AuthResult.Success(principalId = row[AdminsTable.id], role = RoleUtilisateur.Admin)
    }

    fun definirMotDePasseElecteur(electeurId: String, motDePasse: String): Boolean = transaction {
        val majs = ElecteursTable.update({ ElecteursTable.id eq electeurId }) {
            it[ElecteursTable.motDePasseHash] = PasswordHash.hacher(motDePasse)
        }
        majs > 0
    }

    fun creerAdmin(adminId: String, nom: String, ecoleId: String, motDePasse: String) = transaction {
        AdminsTable.insert {
            it[AdminsTable.id] = adminId
            it[AdminsTable.nom] = nom
            it[AdminsTable.ecoleId] = ecoleId
            it[AdminsTable.motDePasseHash] = PasswordHash.hacher(motDePasse)
        }
    }
}

class CodeBasedAuthProvider(private val role: RoleUtilisateur) : AuthenticationProvider {
    override suspend fun authenticate(context: AuthContext): AuthResult = when (role) {
        RoleUtilisateur.Electeur -> AuthRepository.verifierElecteur(context.code, context.motDePasse)
        RoleUtilisateur.Admin -> AuthRepository.verifierAdmin(context.code, context.motDePasse)
    }
}
