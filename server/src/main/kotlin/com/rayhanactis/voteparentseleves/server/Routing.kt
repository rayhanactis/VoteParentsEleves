package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.admin.CreationListe
import com.rayhanactis.voteparentseleves.admin.CreationParent
import com.rayhanactis.voteparentseleves.admin.CreationScrutin
import com.rayhanactis.voteparentseleves.admin.GenerationElecteurs
import com.rayhanactis.voteparentseleves.admin.ImportElecteurs
import com.rayhanactis.voteparentseleves.admin.MiseAJourParametres
import com.rayhanactis.voteparentseleves.admin.ResultatEnvoiMasse
import com.rayhanactis.voteparentseleves.admin.ModificationParent
import com.rayhanactis.voteparentseleves.admin.RenommageScrutin
import com.rayhanactis.voteparentseleves.auth.AuthContext
import com.rayhanactis.voteparentseleves.auth.AuthResult
import com.rayhanactis.voteparentseleves.auth.DemandeLoginAdmin
import com.rayhanactis.voteparentseleves.auth.DemandeLoginElecteur
import com.rayhanactis.voteparentseleves.auth.ReponseToken
import com.rayhanactis.voteparentseleves.model.RoleUtilisateur
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.server.auth.CodeBasedAuthProvider
import com.rayhanactis.voteparentseleves.server.auth.JwtConfig
import com.rayhanactis.voteparentseleves.server.auth.JwtIssuer
import com.rayhanactis.voteparentseleves.server.auth.jwtConfigDepuisEnvironnement
import com.rayhanactis.voteparentseleves.server.db.AdminRepository
import com.rayhanactis.voteparentseleves.server.db.AjoutListeResultat
import com.rayhanactis.voteparentseleves.server.db.ChangementStatutResultat
import com.rayhanactis.voteparentseleves.server.db.ModificationListeResultat
import com.rayhanactis.voteparentseleves.server.db.ParametresRepository
import com.rayhanactis.voteparentseleves.server.db.RepertoireRepository
import com.rayhanactis.voteparentseleves.server.db.ResultatActionParent
import com.rayhanactis.voteparentseleves.server.db.ResultatDepotVote
import com.rayhanactis.voteparentseleves.server.db.ScrutinRepository
import com.rayhanactis.voteparentseleves.server.db.SuppressionResultat
import com.rayhanactis.voteparentseleves.server.db.VoteRepository
import com.rayhanactis.voteparentseleves.server.mail.EmailService
import com.rayhanactis.voteparentseleves.server.mail.ResultatEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.rayhanactis.voteparentseleves.vote.AlgoHare
import com.rayhanactis.voteparentseleves.vote.DemandeVote
import com.rayhanactis.voteparentseleves.vote.RecuVote
import com.rayhanactis.voteparentseleves.vote.StatutEmargement
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val jwt = jwtConfigDepuisEnvironnement()
    configureRateLimiting()
    routing {
        get("/") { call.respondText("VoteParentsEleves API") }

        routesAuth(jwt)
        routesScrutinPublic()
        routesResultats()

        authenticate(REALM_ELECTEUR) {
            routesVote()
        }
        authenticate(REALM_ADMIN) {
            routesScrutinAdmin()
            routesAdmin()
        }
    }
}

fun Route.routesAuth(jwt: JwtConfig) {
    rateLimit(RATE_LIMIT_AUTH) {
        post("/auth/login") {
            val req = runCatching { call.receive<DemandeLoginElecteur>() }.getOrNull()
            if (req == null || req.code.isBlank() || req.scrutinId.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Requête invalide")
                return@post
            }
            val provider = CodeBasedAuthProvider(RoleUtilisateur.Electeur)
            when (val r = provider.authenticate(AuthContext(req.code, req.motDePasse))) {
                is AuthResult.Failure -> call.respond(HttpStatusCode.Unauthorized, r.raison)
                is AuthResult.Success -> {
                    val token = JwtIssuer.pourElecteur(jwt, r.principalId, req.scrutinId)
                    call.respond(ReponseToken(token))
                }
            }
        }

        post("/auth/admin/login") {
            val req = runCatching { call.receive<DemandeLoginAdmin>() }.getOrNull()
            if (req == null || req.code.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Requête invalide")
                return@post
            }
            val provider = CodeBasedAuthProvider(RoleUtilisateur.Admin)
            when (val r = provider.authenticate(AuthContext(req.code, req.motDePasse))) {
                is AuthResult.Failure -> call.respond(HttpStatusCode.Unauthorized, r.raison)
                is AuthResult.Success -> {
                    val token = JwtIssuer.pourAdmin(jwt, r.principalId)
                    call.respond(ReponseToken(token))
                }
            }
        }
    }
}

fun Route.routesVote() {
    get("/scrutins/{id}/mon-vote") {
        val scrutinId = call.parameters["id"]
        if (scrutinId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Identifiant de scrutin manquant")
            return@get
        }
        val principal = call.principal<JWTPrincipal>()!!
        val electeurId = principal.subject
        val scrutinClaim = principal.payload.getClaim(JwtIssuer.CLAIM_SCRUTIN).asString()
        if (electeurId.isNullOrBlank() || scrutinClaim != scrutinId) {
            call.respond(HttpStatusCode.Forbidden, "Token incompatible avec le scrutin demandé")
            return@get
        }
        call.respond(StatutEmargement(VoteRepository.aVote(scrutinId, electeurId)))
    }

    post("/scrutins/{id}/voter") {
        val scrutinId = call.parameters["id"]
        if (scrutinId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Identifiant de scrutin manquant")
            return@post
        }
        val principal = call.principal<JWTPrincipal>()!!
        val electeurId = principal.subject
        val scrutinClaim = principal.payload.getClaim(JwtIssuer.CLAIM_SCRUTIN).asString()
        if (electeurId.isNullOrBlank() || scrutinClaim != scrutinId) {
            call.respond(HttpStatusCode.Forbidden, "Token incompatible avec le scrutin demandé")
            return@post
        }
        val demande = runCatching { call.receive<DemandeVote>() }.getOrNull()
        if (demande == null) {
            call.respond(HttpStatusCode.BadRequest, "Corps de requête invalide")
            return@post
        }
        AdminRepository.reconcilierStatuts()
        when (val r = VoteRepository.deposer(
            scrutinId = scrutinId,
            electeurId = electeurId,
            listeCandidateId = demande.listeCandidateId
        )) {
            is ResultatDepotVote.Succes ->
                call.respond(HttpStatusCode.Created, RecuVote(r.bulletinId, scrutinId))
            ResultatDepotVote.ScrutinInconnu ->
                call.respond(HttpStatusCode.NotFound, "Scrutin '$scrutinId' introuvable")
            ResultatDepotVote.ScrutinNonOuvert ->
                call.respond(HttpStatusCode.Conflict, "Le scrutin n'est pas ouvert au vote")
            ResultatDepotVote.ElecteurInconnu ->
                call.respond(HttpStatusCode.Forbidden, "Électeur inconnu")
            ResultatDepotVote.DejaVote ->
                call.respond(HttpStatusCode.Conflict, "Cet électeur a déjà voté pour ce scrutin")
            ResultatDepotVote.ListeInvalide ->
                call.respond(HttpStatusCode.UnprocessableEntity, "Liste candidate invalide pour ce scrutin")
        }
    }
}

fun Route.routesResultats() {
    get("/scrutins/{id}/resultats") {
        val scrutinId = call.parameters["id"]
        if (scrutinId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Identifiant de scrutin manquant")
            return@get
        }
        val nbSieges = ScrutinRepository.nbSieges(scrutinId)
        if (nbSieges == null) {
            call.respond(HttpStatusCode.NotFound, "Scrutin '$scrutinId' introuvable")
            return@get
        }
        val bulletins = ScrutinRepository.bulletins(scrutinId)
        call.respond(AlgoHare.calculerResultats(bulletins, nbSieges))
    }
}

fun Route.routesScrutinPublic() {
    get("/scrutins") {
        call.respond(AdminRepository.listerScrutins())
    }
    get("/scrutins/{id}") {
        val id = call.parameters["id"].orEmpty()
        val scrutin = AdminRepository.lireScrutin(id)
        if (scrutin == null) call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
        else call.respond(scrutin)
    }
    get("/scrutins/{id}/listes") {
        val id = call.parameters["id"].orEmpty()
        val listes = AdminRepository.listerListes(id)
        if (listes == null) call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
        else call.respond(listes)
    }
}

fun Route.routesScrutinAdmin() {
    post("/scrutins") {
        val input = runCatching { call.receive<CreationScrutin>() }.getOrNull()
        if (input == null ||
            input.nbSieges < 1 ||
            input.dateDebut > input.dateFin
        ) {
            call.respond(HttpStatusCode.BadRequest, "Création de scrutin invalide")
            return@post
        }
        call.respond(HttpStatusCode.Created, AdminRepository.creerScrutin(input))
    }

    post("/scrutins/{id}/listes") {
        val id = call.parameters["id"].orEmpty()
        val input = runCatching { call.receive<CreationListe>() }.getOrNull()
        if (input == null) {
            call.respond(HttpStatusCode.BadRequest, "Corps de requête invalide")
            return@post
        }
        when (val r = AdminRepository.ajouterListe(id, input)) {
            is AjoutListeResultat.Succes -> call.respond(HttpStatusCode.Created, r.liste)
            AjoutListeResultat.ScrutinInconnu ->
                call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
            AjoutListeResultat.ScrutinNonConfigurable ->
                call.respond(HttpStatusCode.Conflict, "Listes modifiables uniquement quand le scrutin est en Configure")
            AjoutListeResultat.DonneesInvalides ->
                call.respond(HttpStatusCode.BadRequest, "Données de liste invalides")
        }
    }

    get("/scrutins/{id}/participation") {
        val id = call.parameters["id"].orEmpty()
        val participation = ScrutinRepository.participation(id)
        if (participation == null) {
            call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
        } else {
            call.respond(participation)
        }
    }

    put("/scrutins/{id}/ouvrir") {
        transitionScrutin(call.parameters["id"].orEmpty(), StatutScrutin.Configure, StatutScrutin.Ouvert)
    }
    put("/scrutins/{id}/fermer") {
        transitionScrutin(call.parameters["id"].orEmpty(), StatutScrutin.Ouvert, StatutScrutin.Ferme)
    }
    put("/scrutins/{id}/depouiller") {
        transitionScrutin(call.parameters["id"].orEmpty(), StatutScrutin.Ferme, StatutScrutin.Depouille)
    }
    put("/scrutins/{id}/programmer") {
        val id = call.parameters["id"].orEmpty()
        repondreChangementStatut(AdminRepository.programmer(id), id)
    }
    put("/scrutins/{id}/deprogrammer") {
        val id = call.parameters["id"].orEmpty()
        repondreChangementStatut(AdminRepository.annulerProgrammation(id), id)
    }

    patch("/scrutins/{id}") {
        val id = call.parameters["id"].orEmpty()
        val input = runCatching { call.receive<RenommageScrutin>() }.getOrNull()
        if (input == null || input.nom.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Le nom est obligatoire")
            return@patch
        }
        when (val r = AdminRepository.renommerScrutin(id, input.nom)) {
            is ChangementStatutResultat.Succes -> call.respond(r.scrutin)
            ChangementStatutResultat.ScrutinInconnu ->
                call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
            is ChangementStatutResultat.TransitionInterdite,
            is ChangementStatutResultat.AutreScrutinOuvert,
            ChangementStatutResultat.AucuneListe ->
                call.respond(HttpStatusCode.Conflict, "Transition interdite")
        }
    }

    delete("/scrutins/{id}") {
        val id = call.parameters["id"].orEmpty()
        when (val r = AdminRepository.supprimerScrutin(id)) {
            SuppressionResultat.Succes -> call.respond(HttpStatusCode.NoContent)
            SuppressionResultat.Introuvable ->
                call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
            is SuppressionResultat.Bloquee ->
                call.respond(HttpStatusCode.Conflict, r.raison)
        }
    }

    delete("/scrutins/{id}/listes/{listeId}") {
        val id = call.parameters["id"].orEmpty()
        val listeId = call.parameters["listeId"].orEmpty()
        when (val r = AdminRepository.supprimerListe(id, listeId)) {
            SuppressionResultat.Succes -> call.respond(HttpStatusCode.NoContent)
            SuppressionResultat.Introuvable ->
                call.respond(HttpStatusCode.NotFound, "Liste introuvable")
            is SuppressionResultat.Bloquee ->
                call.respond(HttpStatusCode.Conflict, r.raison)
        }
    }

    put("/scrutins/{id}/listes/{listeId}") {
        val id = call.parameters["id"].orEmpty()
        val listeId = call.parameters["listeId"].orEmpty()
        val input = runCatching { call.receive<CreationListe>() }.getOrNull()
        if (input == null) {
            call.respond(HttpStatusCode.BadRequest, "Corps de requête invalide")
            return@put
        }
        when (val r = AdminRepository.remplacerListe(
            scrutinId = id,
            listeId = listeId,
            nouveauNom = input.nom,
            nouveauSlogan = input.slogan,
            nouvelleDescription = input.description,
            nouveauxCandidats = input.candidats
        )) {
            is ModificationListeResultat.Succes -> call.respond(r.liste)
            ModificationListeResultat.ScrutinInconnu ->
                call.respond(HttpStatusCode.NotFound, "Scrutin '$id' introuvable")
            ModificationListeResultat.ListeInconnue ->
                call.respond(HttpStatusCode.NotFound, "Liste introuvable")
            ModificationListeResultat.ScrutinNonConfigurable ->
                call.respond(HttpStatusCode.Conflict, "Modification possible uniquement en phase Configure")
            ModificationListeResultat.DonneesInvalides ->
                call.respond(HttpStatusCode.BadRequest, "Données de liste invalides")
        }
    }
}

fun Route.routesAdmin() {
    post("/admin/electeurs/import") {
        val input = runCatching { call.receive<ImportElecteurs>() }.getOrNull()
        if (input == null ||
            input.electeurs.any { it.id.isBlank() || it.ecoleId.isBlank() }
        ) {
            call.respond(HttpStatusCode.BadRequest, "Import invalide")
            return@post
        }
        call.respond(HttpStatusCode.Created, AdminRepository.importerElecteurs(input.electeurs))
    }

    post("/admin/electeurs/generer") {
        val input = runCatching { call.receive<GenerationElecteurs>() }.getOrNull()
        if (input == null || input.electeurs.any { it.nom.isBlank() || it.prenom.isBlank() }) {
            call.respond(HttpStatusCode.BadRequest, "Liste d'électeurs invalide")
            return@post
        }
        call.respond(HttpStatusCode.Created, AdminRepository.genererElecteurs(input.electeurs))
    }


    get("/admin/parents") {
        call.respond(RepertoireRepository.listerParents())
    }

    get("/admin/parents/{id}") {
        val id = call.parameters["id"].orEmpty()
        val parent = RepertoireRepository.lireParent(id)
        if (parent == null) call.respond(HttpStatusCode.NotFound, "Parent introuvable")
        else call.respond(parent)
    }

    post("/admin/parents") {
        val input = runCatching { call.receive<CreationParent>() }.getOrNull()
        if (input == null) {
            call.respond(HttpStatusCode.BadRequest, "Corps de requête invalide")
            return@post
        }
        traiterActionParent(RepertoireRepository.creerParent(input), HttpStatusCode.Created)
    }

    put("/admin/parents/{id}") {
        val id = call.parameters["id"].orEmpty()
        val input = runCatching { call.receive<ModificationParent>() }.getOrNull()
        if (input == null) {
            call.respond(HttpStatusCode.BadRequest, "Corps de requête invalide")
            return@put
        }
        traiterActionParent(RepertoireRepository.modifierParent(id, input))
    }

    delete("/admin/parents/{id}") {
        val id = call.parameters["id"].orEmpty()
        when (val r = RepertoireRepository.supprimerParent(id)) {
            SuppressionResultat.Succes -> call.respond(HttpStatusCode.NoContent)
            SuppressionResultat.Introuvable ->
                call.respond(HttpStatusCode.NotFound, "Parent introuvable")
            is SuppressionResultat.Bloquee ->
                call.respond(HttpStatusCode.Conflict, r.raison)
        }
    }


    post("/admin/parents/envoyer-tous") {
        val params = ParametresRepository.lire()
        if (!EmailService.smtpConfigure(params)) {
            call.respond(HttpStatusCode.Conflict, "Configuration SMTP incomplète (voir Paramètres).")
            return@post
        }
        val parents = RepertoireRepository.listerParents()
        val bilan = withContext(Dispatchers.IO) {
            var envoyes = 0
            var sansEmail = 0
            var echecs = 0
            parents.forEach { p ->
                if (p.email.isBlank()) {
                    sansEmail++
                    return@forEach
                }
                val gen = RepertoireRepository.genererMotDePasse(p.id)
                if (gen == null) {
                    echecs++
                    return@forEach
                }
                val corps = EmailService.corpsIdentifiants(
                    p.prenom, p.nom, params.nomEcole, gen.identifiant, gen.motDePasseClair
                )
                when (EmailService.envoyer(params, p.email, EmailService.SUJET, corps)) {
                    ResultatEmail.Succes -> envoyes++
                    is ResultatEmail.Echec -> echecs++
                }
            }
            ResultatEnvoiMasse(envoyes, sansEmail, echecs)
        }
        call.respond(bilan)
    }

    post("/admin/parents/{id}/mot-de-passe") {
        val id = call.parameters["id"].orEmpty()
        val gen = RepertoireRepository.genererMotDePasse(id)
        if (gen == null) call.respond(HttpStatusCode.NotFound, "Parent introuvable")
        else call.respond(gen)
    }

    post("/admin/parents/{id}/envoyer") {
        val id = call.parameters["id"].orEmpty()
        val parent = RepertoireRepository.lireParent(id)
        if (parent == null) {
            call.respond(HttpStatusCode.NotFound, "Parent introuvable")
            return@post
        }
        if (parent.email.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Ce parent n'a pas d'adresse email.")
            return@post
        }
        val params = ParametresRepository.lire()
        if (!EmailService.smtpConfigure(params)) {
            call.respond(HttpStatusCode.Conflict, "Configuration SMTP incomplète (voir Paramètres).")
            return@post
        }
        val gen = RepertoireRepository.genererMotDePasse(id)
        if (gen == null) {
            call.respond(HttpStatusCode.NotFound, "Parent introuvable")
            return@post
        }
        val corps = EmailService.corpsIdentifiants(
            parent.prenom, parent.nom, params.nomEcole, gen.identifiant, gen.motDePasseClair
        )
        val r = withContext(Dispatchers.IO) {
            EmailService.envoyer(params, parent.email, EmailService.SUJET, corps)
        }
        when (r) {
            ResultatEmail.Succes -> call.respond(gen)
            is ResultatEmail.Echec -> call.respond(HttpStatusCode.BadGateway, "Échec de l'envoi : ${r.raison}")
        }
    }


    get("/admin/parametres") {
        call.respond(ParametresRepository.lire())
    }

    put("/admin/parametres") {
        val input = runCatching { call.receive<MiseAJourParametres>() }.getOrNull()
        if (input == null || input.nomEcole.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Le nom de l'école est obligatoire")
            return@put
        }
        call.respond(ParametresRepository.enregistrer(input))
    }
}

private suspend fun RoutingContext.traiterActionParent(
    r: ResultatActionParent,
    succesStatus: HttpStatusCode = HttpStatusCode.OK
) {
    when (r) {
        is ResultatActionParent.Succes -> call.respond(succesStatus, r.parent)
        ResultatActionParent.Introuvable ->
            call.respond(HttpStatusCode.NotFound, "Parent introuvable")
        is ResultatActionParent.Bloque ->
            call.respond(HttpStatusCode.Conflict, r.raison)
        is ResultatActionParent.DonneesInvalides ->
            call.respond(HttpStatusCode.BadRequest, r.raison)
    }
}

private suspend fun RoutingContext.transitionScrutin(
    scrutinId: String,
    depuis: StatutScrutin,
    vers: StatutScrutin
) {
    repondreChangementStatut(AdminRepository.changerStatut(scrutinId, depuis, vers), scrutinId)
}

private suspend fun RoutingContext.repondreChangementStatut(
    r: ChangementStatutResultat,
    scrutinId: String
) {
    when (r) {
        is ChangementStatutResultat.Succes -> call.respond(r.scrutin)
        ChangementStatutResultat.ScrutinInconnu ->
            call.respond(HttpStatusCode.NotFound, "Scrutin '$scrutinId' introuvable")
        is ChangementStatutResultat.TransitionInterdite ->
            call.respond(
                HttpStatusCode.Conflict,
                "Transition refusée : statut actuel ${r.actuel.javaClass.simpleName}"
            )
        is ChangementStatutResultat.AutreScrutinOuvert ->
            call.respond(
                HttpStatusCode.Conflict,
                "Un autre scrutin est déjà ouvert (${r.nomAutre}). Fermez-le avant d'en ouvrir un autre."
            )
        ChangementStatutResultat.AucuneListe ->
            call.respond(
                HttpStatusCode.Conflict,
                "Ajoutez au moins une liste candidate avant de programmer l'ouverture."
            )
    }
}
