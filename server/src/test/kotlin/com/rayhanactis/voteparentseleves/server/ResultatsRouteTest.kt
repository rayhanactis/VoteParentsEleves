package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.model.ResultatScrutin
import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.BulletinsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.EmargementsTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
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

class ResultatsRouteTest {

    @BeforeTest
    fun setup() {
        Database.connect("jdbc:h2:mem:test_resultats;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                ElecteursTable, AdminsTable, ScrutinsTable, ListesCandidatesTable,
                CandidatsTable, BulletinsTable, EmargementsTable
            )
            ScrutinsTable.insert {
                it[id] = "scr-test"
                it[ecoleId] = "ecole-1"
                it[dateDebut] = 0
                it[dateFin] = 0
                it[nbSieges] = 8
                it[statut] = "Configure"
            }
            insererBulletins("scr-test", "A", 45)
            insererBulletins("scr-test", "B", 35)
            insererBulletins("scr-test", "C", 20)
        }
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

    private fun insererBulletins(scrutin: String, liste: String, n: Int) {
        repeat(n) { i ->
            BulletinsTable.insert {
                it[id] = "b-$liste-$i"
                it[scrutinId] = scrutin
                it[listeCandidateId] = liste
            }
        }
    }

    @Test
    fun `GET resultats renvoie le calcul de Hare pour un scrutin existant`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { routesResultats() }
        }
        val jsonClient = createClient {
            install(ClientContentNegotiation) { json() }
        }

        val response = jsonClient.get("/scrutins/scr-test/resultats")

        assertEquals(HttpStatusCode.OK, response.status)
        val resultat = response.body<ResultatScrutin>()
        assertEquals("scr-test", resultat.scrutinId)
        assertEquals(mapOf("A" to 45, "B" to 35, "C" to 20), resultat.resultatsParListe)
        assertEquals(mapOf("A" to 4, "B" to 3, "C" to 1), resultat.siegesAttribues)
    }

    @Test
    fun `GET resultats renvoie 404 pour un scrutin inconnu`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { routesResultats() }
        }

        val response = client.get("/scrutins/inconnu/resultats")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
