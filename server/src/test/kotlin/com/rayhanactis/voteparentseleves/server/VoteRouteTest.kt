package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.db.BulletinsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.EmargementsTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import com.rayhanactis.voteparentseleves.vote.DemandeVote
import com.rayhanactis.voteparentseleves.vote.RecuVote
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
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
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VoteRouteTest {

    private val scrutinOuvert = "scr-ouvert"
    private val scrutinFerme = "scr-ferme"
    private val electeurValide = "el-1"
    private val electeurAyantDejaVote = "el-2"
    private val listeValide = "liste-A"
    private val listeAutreScrutin = "liste-autre"

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test_vote;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                ElecteursTable,
                com.rayhanactis.voteparentseleves.server.db.AdminsTable,
                ScrutinsTable, ListesCandidatesTable,
                CandidatsTable, BulletinsTable, EmargementsTable
            )

            ScrutinsTable.insert {
                it[id] = scrutinOuvert
                it[ecoleId] = "ecole-1"; it[dateDebut] = 0; it[dateFin] = 0
                it[nbSieges] = 3; it[statut] = "Ouvert"
            }
            ScrutinsTable.insert {
                it[id] = scrutinFerme
                it[ecoleId] = "ecole-1"; it[dateDebut] = 0; it[dateFin] = 0
                it[nbSieges] = 3; it[statut] = "Configure"
            }
            ElecteursTable.insert {
                it[id] = electeurValide; it[nom] = "Dupont"; it[prenom] = "Alice"
                it[ecoleId] = "ecole-1"
            }
            ElecteursTable.insert {
                it[id] = electeurAyantDejaVote; it[nom] = "Martin"; it[prenom] = "Bob"
                it[ecoleId] = "ecole-1"
            }
            ListesCandidatesTable.insert {
                it[id] = listeValide; it[nom] = "Parents Actifs"
                it[scrutinId] = scrutinOuvert
            }
            ListesCandidatesTable.insert {
                it[id] = listeAutreScrutin; it[nom] = "Autre"
                it[scrutinId] = scrutinFerme
            }
            EmargementsTable.insert {
                it[electeurId] = electeurAyantDejaVote
                it[scrutinId] = scrutinOuvert
            }
        }
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(
                BulletinsTable, CandidatsTable, ListesCandidatesTable,
                EmargementsTable, ScrutinsTable, ElecteursTable,
                com.rayhanactis.voteparentseleves.server.db.AdminsTable
            )
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.appSousTest() {
        installAuthTest()
        application {
            install(ContentNegotiation) { json() }
            routing {
                authenticate(REALM_ELECTEUR) { routesVote() }
            }
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.clientJson() =
        createClient { install(ClientContentNegotiation) { json() } }

    @Test
    fun `vote valide pour une liste retourne 201 et enregistre bulletin et emargement`() =
        testApplication {
            appSousTest()
            val client = clientJson()

            val response = client.post("/scrutins/$scrutinOuvert/voter") {
                contentType(ContentType.Application.Json)
                bearer(tokenElecteur(electeurValide, scrutinOuvert))
                setBody(DemandeVote(listeCandidateId = listeValide))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val recu = response.body<RecuVote>()
            assertTrue(recu.bulletinId.startsWith("blt-"))
            assertEquals(scrutinOuvert, recu.scrutinId)

            transaction {
                val bulletins = BulletinsTable.selectAll()
                    .where { BulletinsTable.scrutinId eq scrutinOuvert }
                    .toList()
                assertEquals(1, bulletins.size)
                assertEquals(listeValide, bulletins.single()[BulletinsTable.listeCandidateId])

                val emargements = EmargementsTable.selectAll()
                    .where { EmargementsTable.electeurId eq electeurValide }
                    .toList()
                assertEquals(1, emargements.size)
            }
        }

    @Test
    fun `vote blanc retourne 201 et enregistre un bulletin avec liste null`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurValide, scrutinOuvert))
            setBody(DemandeVote(listeCandidateId = null))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        transaction {
            val bulletin = BulletinsTable.selectAll()
                .where { BulletinsTable.scrutinId eq scrutinOuvert }
                .single()
            assertNull(bulletin[BulletinsTable.listeCandidateId])
        }
    }

    @Test
    fun `electeur deja emarge ne peut pas voter une seconde fois`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurAyantDejaVote, scrutinOuvert))
            setBody(DemandeVote(listeCandidateId = listeValide))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `scrutin non ouvert refuse le vote`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinFerme/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurValide, scrutinFerme))
            setBody(DemandeVote(listeCandidateId = null))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `scrutin inconnu retourne 404`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/inexistant/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurValide, "inexistant"))
            setBody(DemandeVote(listeCandidateId = null))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `electeur inconnu en DB retourne 403`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur("inconnu", scrutinOuvert))
            setBody(DemandeVote(listeCandidateId = null))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `liste appartenant a un autre scrutin retourne 422`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurValide, scrutinOuvert))
            setBody(DemandeVote(listeCandidateId = listeAutreScrutin))
        }

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `sans token JWT le vote est refuse en 401`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            setBody(DemandeVote(listeCandidateId = listeValide))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `token avec scrutinId different du path est refuse en 403`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/scrutins/$scrutinOuvert/voter") {
            contentType(ContentType.Application.Json)
            bearer(tokenElecteur(electeurValide, "autre-scrutin"))
            setBody(DemandeVote(listeCandidateId = listeValide))
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
