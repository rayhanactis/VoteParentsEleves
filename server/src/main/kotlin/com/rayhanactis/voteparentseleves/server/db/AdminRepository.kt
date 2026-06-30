package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.admin.CreationElecteur
import com.rayhanactis.voteparentseleves.admin.CreationListe
import com.rayhanactis.voteparentseleves.admin.CreationScrutin
import com.rayhanactis.voteparentseleves.admin.ElecteurGenere
import com.rayhanactis.voteparentseleves.admin.LigneElecteurBrute
import com.rayhanactis.voteparentseleves.admin.ResultatImport
import com.rayhanactis.voteparentseleves.model.Candidat
import com.rayhanactis.voteparentseleves.server.auth.PasswordHash
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.model.code
import com.rayhanactis.voteparentseleves.model.statutDepuisCode
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

sealed class AjoutListeResultat {
    data class Succes(val liste: ListeCandidate) : AjoutListeResultat()
    data object ScrutinInconnu : AjoutListeResultat()
    data object ScrutinNonConfigurable : AjoutListeResultat()
    data object DonneesInvalides : AjoutListeResultat()
}

sealed class ChangementStatutResultat {
    data class Succes(val scrutin: Scrutin) : ChangementStatutResultat()
    data object ScrutinInconnu : ChangementStatutResultat()
    data class TransitionInterdite(val actuel: StatutScrutin) : ChangementStatutResultat()
    data class AutreScrutinOuvert(val nomAutre: String) : ChangementStatutResultat()
}

sealed class SuppressionResultat {
    data object Succes : SuppressionResultat()
    data object Introuvable : SuppressionResultat()
    data class Bloquee(val raison: String) : SuppressionResultat()
}

sealed class ModificationListeResultat {
    data class Succes(val liste: ListeCandidate) : ModificationListeResultat()
    data object ScrutinInconnu : ModificationListeResultat()
    data object ListeInconnue : ModificationListeResultat()
    data object ScrutinNonConfigurable : ModificationListeResultat()
    data object DonneesInvalides : ModificationListeResultat()
}

object AdminRepository {

    private fun rowToScrutin(row: ResultRow): Scrutin = Scrutin(
        id = row[ScrutinsTable.id],
        ecoleId = row[ScrutinsTable.ecoleId],
        nom = row[ScrutinsTable.nom],
        dateDebut = row[ScrutinsTable.dateDebut],
        dateFin = row[ScrutinsTable.dateFin],
        nbSieges = row[ScrutinsTable.nbSieges],
        statut = statutDepuisCode(row[ScrutinsTable.statut])
            ?: error("Statut BDD corrompu: ${row[ScrutinsTable.statut]}")
    )

    fun creerScrutin(input: CreationScrutin): Scrutin = transaction {
        val ecoleEffective = input.ecoleId.ifBlank { ParametresRepository.codeEcoleActuel() }
        val nouveau = Scrutin(
            id = "scr-${UUID.randomUUID()}",
            ecoleId = ecoleEffective,
            nom = input.nom,
            dateDebut = input.dateDebut,
            dateFin = input.dateFin,
            nbSieges = input.nbSieges,
            statut = StatutScrutin.Configure
        )
        ScrutinsTable.insert {
            it[id] = nouveau.id
            it[ecoleId] = nouveau.ecoleId
            it[nom] = nouveau.nom
            it[dateDebut] = nouveau.dateDebut
            it[dateFin] = nouveau.dateFin
            it[nbSieges] = nouveau.nbSieges
            it[statut] = nouveau.statut.code()
        }
        nouveau
    }

    fun renommerScrutin(scrutinId: String, nouveauNom: String): ChangementStatutResultat = transaction {
        val nbMaj = ScrutinsTable.update({ ScrutinsTable.id eq scrutinId }) {
            it[nom] = nouveauNom.trim()
        }
        if (nbMaj == 0) return@transaction ChangementStatutResultat.ScrutinInconnu
        val miseAJour = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map(::rowToScrutin)
            .single()
        ChangementStatutResultat.Succes(miseAJour)
    }

    fun supprimerScrutin(scrutinId: String): SuppressionResultat = transaction {
        val existe = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .empty().not()
        if (!existe) return@transaction SuppressionResultat.Introuvable

        val nbBulletins = BulletinsTable.selectAll()
            .where { BulletinsTable.scrutinId eq scrutinId }
            .count()
        if (nbBulletins > 0) {
            return@transaction SuppressionResultat.Bloquee(
                "Ce scrutin a reçu $nbBulletins bulletin(s). Suppression refusée pour préserver l'intégrité de l'élection."
            )
        }

        EmargementsTable.deleteWhere { EmargementsTable.scrutinId eq scrutinId }
        val listesIds = ListesCandidatesTable.selectAll()
            .where { ListesCandidatesTable.scrutinId eq scrutinId }
            .map { it[ListesCandidatesTable.id] }
        if (listesIds.isNotEmpty()) {
            CandidatsTable.deleteWhere { CandidatsTable.listeId inList listesIds }
            ListesCandidatesTable.deleteWhere { ListesCandidatesTable.scrutinId eq scrutinId }
        }
        ScrutinsTable.deleteWhere { ScrutinsTable.id eq scrutinId }
        SuppressionResultat.Succes
    }

    fun supprimerListe(scrutinId: String, listeId: String): SuppressionResultat = transaction {
        val statut = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.statut] }
            .singleOrNull() ?: return@transaction SuppressionResultat.Introuvable
        if (statut != StatutScrutin.Configure.code()) {
            return@transaction SuppressionResultat.Bloquee(
                "Le scrutin est ouvert ou clos, plus possible de modifier les listes."
            )
        }
        val existe = ListesCandidatesTable.selectAll()
            .where {
                (ListesCandidatesTable.id eq listeId) and
                    (ListesCandidatesTable.scrutinId eq scrutinId)
            }
            .empty().not()
        if (!existe) return@transaction SuppressionResultat.Introuvable
        CandidatsTable.deleteWhere { CandidatsTable.listeId eq listeId }
        ListesCandidatesTable.deleteWhere { ListesCandidatesTable.id eq listeId }
        SuppressionResultat.Succes
    }

    fun remplacerListe(
        scrutinId: String,
        listeId: String,
        nouveauNom: String,
        nouveauSlogan: String,
        nouvelleDescription: String,
        nouveauxCandidats: List<com.rayhanactis.voteparentseleves.admin.CreationCandidat>
    ): ModificationListeResultat = transaction {
        if (nouveauNom.isBlank() || nouveauxCandidats.isEmpty() ||
            nouveauxCandidats.any { it.nom.isBlank() || it.prenom.isBlank() }
        ) {
            return@transaction ModificationListeResultat.DonneesInvalides
        }
        val statut = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.statut] }
            .singleOrNull() ?: return@transaction ModificationListeResultat.ScrutinInconnu
        if (statut != StatutScrutin.Configure.code()) {
            return@transaction ModificationListeResultat.ScrutinNonConfigurable
        }
        val existe = ListesCandidatesTable.selectAll()
            .where {
                (ListesCandidatesTable.id eq listeId) and
                    (ListesCandidatesTable.scrutinId eq scrutinId)
            }
            .empty().not()
        if (!existe) return@transaction ModificationListeResultat.ListeInconnue

        ListesCandidatesTable.update({ ListesCandidatesTable.id eq listeId }) {
            it[nom] = nouveauNom.trim()
            it[slogan] = nouveauSlogan.trim()
            it[description] = nouvelleDescription.trim()
        }
        CandidatsTable.deleteWhere { CandidatsTable.listeId eq listeId }
        val candidats = nouveauxCandidats.map { c ->
            val candidatId = "cnd-${UUID.randomUUID()}"
            CandidatsTable.insert {
                it[id] = candidatId
                it[nom] = c.nom
                it[prenom] = c.prenom
                it[electeurId] = c.electeurId
                it[CandidatsTable.listeId] = listeId
            }
            Candidat(
                id = candidatId,
                nom = c.nom,
                prenom = c.prenom,
                listeCandidateId = listeId
            )
        }
        ModificationListeResultat.Succes(
            ListeCandidate(
                id = listeId,
                nom = nouveauNom.trim(),
                candidats = candidats,
                scrutinId = scrutinId,
                slogan = nouveauSlogan.trim(),
                description = nouvelleDescription.trim()
            )
        )
    }

    fun listerScrutins(): List<Scrutin> = transaction {
        ScrutinsTable.selectAll().map(::rowToScrutin)
    }

    fun lireScrutin(scrutinId: String): Scrutin? = transaction {
        ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map(::rowToScrutin)
            .singleOrNull()
    }

    fun listerListes(scrutinId: String): List<ListeCandidate>? = transaction {
        val scrutinExiste = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .empty().not()
        if (!scrutinExiste) return@transaction null

        val listes = ListesCandidatesTable.selectAll()
            .where { ListesCandidatesTable.scrutinId eq scrutinId }
            .map {
                ListeBrute(
                    id = it[ListesCandidatesTable.id],
                    nom = it[ListesCandidatesTable.nom],
                    slogan = it[ListesCandidatesTable.slogan],
                    description = it[ListesCandidatesTable.description],
                    scrutinId = it[ListesCandidatesTable.scrutinId]
                )
            }

        val idsListes = listes.map { it.id }
        val candidatsParListe = if (idsListes.isEmpty()) emptyMap()
        else CandidatsTable.selectAll()
            .where { CandidatsTable.listeId inList idsListes }
            .map {
                Candidat(
                    id = it[CandidatsTable.id],
                    nom = it[CandidatsTable.nom],
                    prenom = it[CandidatsTable.prenom],
                    listeCandidateId = it[CandidatsTable.listeId]
                )
            }
            .groupBy { it.listeCandidateId }

        listes.map { liste ->
            ListeCandidate(
                id = liste.id,
                nom = liste.nom,
                candidats = candidatsParListe[liste.id].orEmpty(),
                scrutinId = liste.scrutinId,
                slogan = liste.slogan,
                description = liste.description
            )
        }
    }

    private data class ListeBrute(
        val id: String,
        val nom: String,
        val slogan: String,
        val description: String,
        val scrutinId: String
    )

    fun ajouterListe(scrutinId: String, input: CreationListe): AjoutListeResultat = transaction {
        if (input.nom.isBlank() || input.candidats.isEmpty() ||
            input.candidats.any { it.nom.isBlank() || it.prenom.isBlank() }
        ) {
            return@transaction AjoutListeResultat.DonneesInvalides
        }
        val statut = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map { it[ScrutinsTable.statut] }
            .singleOrNull() ?: return@transaction AjoutListeResultat.ScrutinInconnu

        if (statut != StatutScrutin.Configure.code()) {
            return@transaction AjoutListeResultat.ScrutinNonConfigurable
        }

        val listeId = "lst-${UUID.randomUUID()}"
        ListesCandidatesTable.insert {
            it[id] = listeId
            it[nom] = input.nom
            it[slogan] = input.slogan.trim()
            it[description] = input.description.trim()
            it[ListesCandidatesTable.scrutinId] = scrutinId
        }
        val candidats = input.candidats.map { c ->
            val candidatId = "cnd-${UUID.randomUUID()}"
            CandidatsTable.insert {
                it[id] = candidatId
                it[nom] = c.nom
                it[prenom] = c.prenom
                it[electeurId] = c.electeurId
                it[CandidatsTable.listeId] = listeId
            }
            Candidat(
                id = candidatId,
                nom = c.nom,
                prenom = c.prenom,
                listeCandidateId = listeId
            )
        }
        AjoutListeResultat.Succes(
            ListeCandidate(
                id = listeId,
                nom = input.nom,
                candidats = candidats,
                scrutinId = scrutinId,
                slogan = input.slogan.trim(),
                description = input.description.trim()
            )
        )
    }

    fun changerStatut(
        scrutinId: String,
        depuis: StatutScrutin,
        vers: StatutScrutin
    ): ChangementStatutResultat = transaction {
        val scrutin = ScrutinsTable.selectAll()
            .where { ScrutinsTable.id eq scrutinId }
            .map(::rowToScrutin)
            .singleOrNull() ?: return@transaction ChangementStatutResultat.ScrutinInconnu

        if (scrutin.statut != depuis) {
            return@transaction ChangementStatutResultat.TransitionInterdite(scrutin.statut)
        }

        if (vers == StatutScrutin.Ouvert) {
            val autreOuvert = ScrutinsTable.selectAll()
                .where { ScrutinsTable.statut eq StatutScrutin.Ouvert.code() }
                .map(::rowToScrutin)
                .firstOrNull { it.id != scrutinId }
            if (autreOuvert != null) {
                return@transaction ChangementStatutResultat.AutreScrutinOuvert(
                    autreOuvert.nom.ifBlank { autreOuvert.id }
                )
            }
        }

        ScrutinsTable.update({ ScrutinsTable.id eq scrutinId }) {
            it[statut] = vers.code()
        }
        ChangementStatutResultat.Succes(scrutin.copy(statut = vers))
    }

    fun importerElecteurs(electeurs: List<CreationElecteur>): ResultatImport = transaction {
        if (electeurs.isEmpty()) return@transaction ResultatImport(0, 0)
        val ids = electeurs.map { it.id }
        val existants = ElecteursTable.selectAll()
            .where { ElecteursTable.id inList ids }
            .map { it[ElecteursTable.id] }
            .toSet()
        val (aIgnorer, aInserer) = electeurs.partition { it.id in existants }
        aInserer.forEach { e ->
            ElecteursTable.insert {
                it[id] = e.id
                it[nom] = e.nom
                it[prenom] = e.prenom
                it[ecoleId] = e.ecoleId
            }
        }
        ResultatImport(ajoutes = aInserer.size, ignores = aIgnorer.size)
    }

    fun genererElecteurs(lignes: List<LigneElecteurBrute>): List<ElecteurGenere> = transaction {
        if (lignes.isEmpty()) return@transaction emptyList()
        val ecoleId = ParametresRepository.codeEcoleActuel()
        val dejaUtilises = ElecteursTable.selectAll().map { it[ElecteursTable.id] }.toMutableSet()
        lignes.map { ligne ->
            val id = genererIdentifiantUnique(ligne.prenom, ligne.nom, dejaUtilises)
            dejaUtilises += id
            val motDePasse = genererMotDePasseLisible()
            ElecteursTable.insert {
                it[ElecteursTable.id] = id
                it[ElecteursTable.nom] = ligne.nom.trim()
                it[ElecteursTable.prenom] = ligne.prenom.trim()
                it[ElecteursTable.ecoleId] = ecoleId
                it[ElecteursTable.motDePasseHash] = PasswordHash.hacher(motDePasse)
            }
            ElecteurGenere(
                id = id,
                nom = ligne.nom.trim(),
                prenom = ligne.prenom.trim(),
                motDePasseClair = motDePasse,
                ecoleId = ecoleId
            )
        }
    }
}
