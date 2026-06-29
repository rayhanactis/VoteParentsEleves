package com.rayhanactis.voteparentseleves.server

import com.auth0.jwt.JWT
import com.rayhanactis.voteparentseleves.auth.DemandeLoginAdmin
import com.rayhanactis.voteparentseleves.auth.DemandeLoginElecteur
import com.rayhanactis.voteparentseleves.auth.ReponseToken
import com.rayhanactis.voteparentseleves.server.auth.AuthRepository
import com.rayhanactis.voteparentseleves.server.auth.JwtIssuer
import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.BulletinsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.EmargementsTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthFlowTest {

    private val electeurId = "el-42"
    private val electeurMdp = "secret-electeur"
    private val adminId = "admin-1"
    private val adminMdp = "secret-admin"
    private val scrutinId = "scr-1"

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test_auth;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                ElecteursTable, AdminsTable, ScrutinsTable, ListesCandidatesTable,
                CandidatsTable, BulletinsTable, EmargementsTable
            )
            ElecteursTable.insert {
                it[id] = electeurId; it[nom] = "Dupont"; it[prenom] = "Alice"
                it[ecoleId] = "ecole-1"
            }
            ScrutinsTable.insert {
                it[id] = scrutinId
                it[ecoleId] = "ecole-1"; it[dateDebut] = 0; it[dateFin] = 0
                it[nbSieges] = 3; it[statut] = "Ouvert"
            }
        }
        AuthRepository.creerAdmin(adminId, "Admin", "ecole-1", adminMdp)
        AuthRepository.definirMotDePasseElecteur(electeurId, electeurMdp)
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(
                BulletinsTable, CandidatsTable, ListesCandidatesTable,
                EmargementsTable, ScrutinsTable, ElecteursTable, AdminsTable
            )
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.appSousTest() {
        application {
            install(ContentNegotiation) { json() }
            configureRateLimiting()
            routing { routesAuth(testJwtConfig) }
        }
    }

    private fun io.ktor.server.testing.ApplicationTestBuilder.clientJson() =
        createClient { install(ClientContentNegotiation) { json() } }

    @Test
    fun `login electeur avec bons identifiants retourne un JWT decodable`() = testApplication {
        appSousTest()
        val client = clientJson()

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(DemandeLoginElecteur(electeurId, electeurMdp, scrutinId))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val token = response.body<ReponseToken>().token
        val decoded = JWT.decode(token)
        assertEquals(electeurId, decoded.subject)
        assertEquals(scrutinId, decoded.getClaim(JwtIssuer.CLAIM_SCRUTIN).asString())
        assertTrue(decoded.audience.contains(testJwtConfig.electeurAudience))
    }

    @Test
    fun `login electeur avec mauvais mot de passe retourne 401`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(DemandeLoginElecteur(electeurId, "mauvais", scrutinId))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login electeur avec code inconnu retourne 401`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(DemandeLoginElecteur("inconnu", electeurMdp, scrutinId))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login admin avec bons identifiants retourne un JWT admin`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/auth/admin/login") {
            contentType(ContentType.Application.Json)
            setBody(DemandeLoginAdmin(adminId, adminMdp))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val token = response.body<ReponseToken>().token
        val decoded = JWT.decode(token)
        assertEquals(adminId, decoded.subject)
        assertTrue(decoded.audience.contains(testJwtConfig.adminAudience))
    }

    @Test
    fun `login admin avec mauvais identifiants retourne 401`() = testApplication {
        appSousTest()
        val client = clientJson()
        val response = client.post("/auth/admin/login") {
            contentType(ContentType.Application.Json)
            setBody(DemandeLoginAdmin(adminId, "faux"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `plus de 5 tentatives de login par minute retourne 429`() = testApplication {
        appSousTest()
        val client = clientJson()

        val statuts = (1..6).map {
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(DemandeLoginElecteur(electeurId, "mauvais", scrutinId))
            }.status
        }

        assertEquals(HttpStatusCode.TooManyRequests, statuts.last())
        assertTrue(statuts.dropLast(1).all { it == HttpStatusCode.Unauthorized })
    }
}
