package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.admin.CreationCandidat
import com.rayhanactis.voteparentseleves.admin.CreationElecteur
import com.rayhanactis.voteparentseleves.admin.CreationListe
import com.rayhanactis.voteparentseleves.admin.CreationScrutin
import com.rayhanactis.voteparentseleves.admin.ElecteurGenere
import com.rayhanactis.voteparentseleves.admin.GenerationElecteurs
import com.rayhanactis.voteparentseleves.admin.ImportElecteurs
import com.rayhanactis.voteparentseleves.admin.LigneElecteurBrute
import com.rayhanactis.voteparentseleves.admin.ResultatImport
import com.rayhanactis.voteparentseleves.model.ListeCandidate
import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.model.Scrutin
import com.rayhanactis.voteparentseleves.model.StatutScrutin
import com.rayhanactis.voteparentseleves.server.auth.AuthRepository
import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.BulletinsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.EmargementsTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ParametresEcoleTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import com.rayhanactis.voteparentseleves.vote.DemandeVote
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminFlowTest {

    private val adminId = "admin-1"
    private val motDePasseElecteur = "secret"

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test_admin;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                ElecteursTable, AdminsTable, ScrutinsTable, ListesCandidatesTable,
                CandidatsTable, BulletinsTable, EmargementsTable, ParametresEcoleTable
            )
        }
        AuthRepository.creerAdmin(adminId, "Admin", "ecole-1", "admin-secret")
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(
                BulletinsTable, CandidatsTable, ListesCandidatesTable,
                EmargementsTable, ScrutinsTable, ElecteursTable, AdminsTable, ParametresEcoleTable
            )
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.appSousTest() {
        installAuthTest()
        application {
            install(ContentNegotiation) { json() }
            configureRateLimiting()
            routing {
                routesAuth(testJwtConfig)
                routesScrutinPublic()
                routesResultats()
                authenticate(REALM_ELECTEUR) { routesVote() }
                authenticate(REALM_ADMIN) {
                    routesScrutinAdmin()
                    routesAdmin()
                }
            }
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.clientJson() =
        createClient { install(ClientContentNegotiation) { json() } }

    @Test
    fun `flux complet de bout en bout - creer, configurer, voter, depouiller`() = testApplication {
        appSousTest()
        val client = clientJson()
        val admin = tokenAdmin(adminId)

        val scrutin = client.post("/scrutins") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationScrutin(ecoleId = "ecole-1", dateDebut = 100, dateFin = 200, nbSieges = 8))
        }.let {
            assertEquals(HttpStatusCode.Created, it.status)
            it.body<Scrutin>()
        }
        assertEquals(StatutScrutin.Configure, scrutin.statut)

        val listeA = client.post("/scrutins/${scrutin.id}/listes") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationListe(
                nom = "Parents Actifs",
                candidats = listOf(CreationCandidat("Dupont", "Alice"), CreationCandidat("Roy", "Hugo"))
            ))
        }.let {
            assertEquals(HttpStatusCode.Created, it.status)
            it.body<ListeCandidate>()
        }
        val listeB = client.post("/scrutins/${scrutin.id}/listes") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationListe("Parents Solidaires", listOf(CreationCandidat("Martin", "Bob"))))
        }.body<ListeCandidate>()

        val listes = client.get("/scrutins/${scrutin.id}/listes").body<List<ListeCandidate>>()
        assertEquals(2, listes.size)
        assertEquals(2, listes.single { it.id == listeA.id }.candidats.size)

        val electeurs = (1..3).map { i ->
            CreationElecteur(id = "el-$i", nom = "N$i", prenom = "P$i", ecoleId = "ecole-1")
        }
        val import = client.post("/admin/electeurs/import") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(ImportElecteurs(electeurs))
        }.let {
            assertEquals(HttpStatusCode.Created, it.status)
            it.body<ResultatImport>()
        }
        assertEquals(ResultatImport(3, 0), import)

        electeurs.forEach { AuthRepository.definirMotDePasseElecteur(it.id, motDePasseElecteur) }

        val ouvert = client.put("/scrutins/${scrutin.id}/ouvrir") { bearer(admin) }.body<Scrutin>()
        assertEquals(StatutScrutin.Ouvert, ouvert.statut)

        val ajoutTardif = client.post("/scrutins/${scrutin.id}/listes") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationListe("Trop tard", listOf(CreationCandidat("X", "Y"))))
        }
        assertEquals(HttpStatusCode.Conflict, ajoutTardif.status)

        val votes = listOf("el-1" to listeA.id, "el-2" to listeA.id, "el-3" to listeB.id)
        votes.forEach { (electeurId, listeId) ->
            val r = client.post("/scrutins/${scrutin.id}/voter") {
                contentType(ContentType.Application.Json)
                bearer(tokenElecteur(electeurId, scrutin.id))
                setBody(DemandeVote(listeCandidateId = listeId))
            }
            assertEquals(HttpStatusCode.Created, r.status)
        }

        val ferme = client.put("/scrutins/${scrutin.id}/fermer") { bearer(admin) }.body<Scrutin>()
        assertEquals(StatutScrutin.Ferme, ferme.statut)

        val resultat = client.get("/scrutins/${scrutin.id}/resultats").body<ResultatScrutin>()
        assertEquals(mapOf(listeA.id to 2, listeB.id to 1), resultat.resultatsParListe)
        assertEquals(mapOf(listeA.id to 1, listeB.id to 1), resultat.siegesAttribues)
        assertTrue(resultat.procesVerbal.contains("Sièges attribués : 2 / 8"))

        val depouille = client.put("/scrutins/${scrutin.id}/depouiller") { bearer(admin) }.body<Scrutin>()
        assertEquals(StatutScrutin.Depouille, depouille.statut)
    }

    @Test
    fun `depouiller un scrutin non ferme retourne 409`() = testApplication {
        appSousTest()
        val client = clientJson()
        val admin = tokenAdmin(adminId)
        val scrutin = client.post("/scrutins") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationScrutin("ecole-1", 0, 0, 3))
        }.body<Scrutin>()

        val response = client.put("/scrutins/${scrutin.id}/depouiller") { bearer(admin) }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `generation d'identifiants cree des codes uniques et des mots de passe utilisables`() = testApplication {
        appSousTest()
        val client = clientJson()
        val admin = tokenAdmin(adminId)

        val lignes = listOf(
            LigneElecteurBrute(nom = "Dupont", prenom = "Alice"),
            LigneElecteurBrute(nom = "Dupont", prenom = "Alice"),
            LigneElecteurBrute(nom = "Élève", prenom = "Émilie")
        )
        val generes = client.post("/admin/electeurs/generer") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(GenerationElecteurs(lignes))
        }.let {
            assertEquals(HttpStatusCode.Created, it.status)
            it.body<List<ElecteurGenere>>()
        }

        assertEquals(3, generes.size)
        assertEquals(3, generes.map { it.id }.toSet().size)
        assertTrue(generes[0].id == "alice.dupont")
        assertTrue(generes[1].id == "alice.dupont2")
        assertTrue(generes[2].id == "emilie.eleve")
        generes.forEach { assertEquals(6, it.motDePasseClair.length) }

        val login = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                com.rayhanactis.voteparentseleves.auth.DemandeLoginElecteur(
                    code = generes[0].id,
                    motDePasse = generes[0].motDePasseClair,
                    scrutinId = "scr-peu-importe"
                )
            )
        }
        assertEquals(HttpStatusCode.OK, login.status)
    }

    @Test
    fun `creation de scrutin sans token admin retourne 401`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/scrutins") {
            contentType(ContentType.Application.Json)
            setBody(CreationScrutin("ecole-1", 0, 100, 3))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `creation de scrutin avec donnees invalides retourne 400`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/scrutins") {
            contentType(ContentType.Application.Json); bearer(tokenAdmin(adminId))
            setBody(CreationScrutin(ecoleId = "", dateDebut = 200, dateFin = 100, nbSieges = 0))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `fermer un scrutin non ouvert retourne 409`() = testApplication {
        appSousTest()
        val client = clientJson()
        val admin = tokenAdmin(adminId)
        val scrutin = client.post("/scrutins") {
            contentType(ContentType.Application.Json); bearer(admin)
            setBody(CreationScrutin("ecole-1", 0, 0, 3))
        }.body<Scrutin>()

        val response = client.put("/scrutins/${scrutin.id}/fermer") { bearer(admin) }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `import reimporte le meme electeur sans doublon`() = testApplication {
        appSousTest()
        val client = clientJson()
        val admin = tokenAdmin(adminId)
        val e = ImportElecteurs(listOf(CreationElecteur("el-1", "A", "B", "ec")))
        val r1 = client.post("/admin/electeurs/import") {
            contentType(ContentType.Application.Json); bearer(admin); setBody(e)
        }.body<ResultatImport>()
        val r2 = client.post("/admin/electeurs/import") {
            contentType(ContentType.Application.Json); bearer(admin); setBody(e)
        }.body<ResultatImport>()
        assertEquals(ResultatImport(1, 0), r1)
        assertEquals(ResultatImport(0, 1), r2)
    }
}
