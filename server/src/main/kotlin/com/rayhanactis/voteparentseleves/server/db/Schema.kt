package com.rayhanactis.voteparentseleves.server.db

import org.jetbrains.exposed.sql.Table

object ElecteursTable : Table("electeurs") {
    val id = varchar("id", 50)
    val nom = varchar("nom", 100)
    val prenom = varchar("prenom", 100)
    val ecoleId = varchar("ecole_id", 50)
    val email = varchar("email", 200).default("")
    val aVote = bool("a_vote").default(false)
    val motDePasseHash = varchar("mot_de_passe_hash", 200).default("")

    override val primaryKey = PrimaryKey(id)
}

object AdminsTable : Table("admins") {
    val id = varchar("id", 50)
    val nom = varchar("nom", 100)
    val ecoleId = varchar("ecole_id", 50)
    val motDePasseHash = varchar("mot_de_passe_hash", 200)

    override val primaryKey = PrimaryKey(id)
}

object ScrutinsTable : Table("scrutins") {
    val id = varchar("id", 50)
    val ecoleId = varchar("ecole_id", 50)
    val nom = varchar("nom", 200).default("")
    val dateDebut = long("date_debut")
    val dateFin = long("date_fin")
    val nbSieges = integer("nb_sieges")
    val statut = varchar("statut", 20) // "Configure", "Ouvert", "Ferme", "Depouille"

    override val primaryKey = PrimaryKey(id)
}

object ListesCandidatesTable : Table("listes_candidates") {
    val id = varchar("id", 50)
    val nom = varchar("nom", 200)
    val slogan = varchar("slogan", 300).default("")
    val description = text("description").default("")
    val scrutinId = varchar("scrutin_id", 50) references ScrutinsTable.id

    override val primaryKey = PrimaryKey(id)
}

object CandidatsTable : Table("candidats") {
    val id = varchar("id", 50)
    val nom = varchar("nom", 100)
    val prenom = varchar("prenom", 100)
    // Lien optionnel vers le parent du répertoire (renseigné dès qu'un candidat
    // est sélectionné via le répertoire des parents d'élèves).
    val electeurId = varchar("electeur_id", 50).references(ElecteursTable.id).nullable()
    val listeId = varchar("liste_id", 50) references ListesCandidatesTable.id

    override val primaryKey = PrimaryKey(id)
}

// Table Anonymisée : On ne sait pas qui a déposé quel bulletin
object BulletinsTable : Table("bulletins") {
    val id = varchar("id", 50)
    val scrutinId = varchar("scrutin_id", 50) references ScrutinsTable.id
    val listeCandidateId = varchar("liste_candidate_id", 50).nullable() // null = vote blanc
    
    override val primaryKey = PrimaryKey(id)
}

// Table d'émargement : On sait qui a voté pour quel scrutin, mais pas pour quelle liste
object EmargementsTable : Table("emargements") {
    val electeurId = varchar("electeur_id", 50) references ElecteursTable.id
    val scrutinId = varchar("scrutin_id", 50) references ScrutinsTable.id
    // Horodatage du dépôt (epoch millis) pour le suivi de participation en
    // temps réel. default(0) pour migrer les émargements déjà présents.
    val dateVote = long("date_vote").default(0L)

    override val primaryKey = PrimaryKey(electeurId, scrutinId)
}

// Paramètres établissement — table singleton (toujours id = 1)
object ParametresEcoleTable : Table("parametres_ecole") {
    val id = integer("id").default(1)
    val nomEcole = varchar("nom_ecole", 200).default("")
    val codePostal = varchar("code_postal", 10).default("")
    val codeEcole = varchar("code_ecole", 50).default("ecole-default")
    // Config SMTP pour l'envoi des identifiants par email.
    val emailExpediteur = varchar("email_expediteur", 200).default("")
    val smtpHost = varchar("smtp_host", 200).default("")
    val smtpPort = integer("smtp_port").default(587)
    val smtpMotDePasse = varchar("smtp_mot_de_passe", 300).default("")

    override val primaryKey = PrimaryKey(id)
}
