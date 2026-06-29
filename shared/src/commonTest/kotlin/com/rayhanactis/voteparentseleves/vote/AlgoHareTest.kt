package com.rayhanactis.voteparentseleves.vote

import com.rayhanactis.voteparentseleves.model.Bulletin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlgoHareTest {

    private val scrutinId = "scr-1"

    private fun bulletins(vararg blocs: Pair<String?, Int>): List<Bulletin> =
        blocs.flatMapIndexed { blocIdx, (liste, n) ->
            (0 until n).map { i ->
                Bulletin(
                    id = "b-$blocIdx-$i",
                    scrutinId = scrutinId,
                    listeCandidateId = liste
                )
            }
        }

    @Test
    fun `repartition entiere sans reste`() {
        // 100 votes, 10 sièges, quotient = 10, tous les restes sont nuls
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 50, "B" to 30, "C" to 20),
            nbSieges = 10
        )

        assertEquals(scrutinId, res.scrutinId)
        assertEquals(mapOf("A" to 50, "B" to 30, "C" to 20), res.resultatsParListe)
        assertEquals(mapOf("A" to 5, "B" to 3, "C" to 2), res.siegesAttribues)
        assertEquals(10, res.siegesAttribues.values.sum())
    }

    @Test
    fun `repartition avec plus forts restes`() {
        // 100 votes, 8 sièges, quotient = 12
        // A 45 → 3 sièges (reste 9), B 35 → 2 (reste 11), C 20 → 1 (reste 8)
        // Sièges entiers = 6, 2 sièges restants → B (reste 11) puis A (reste 9)
        // Final : A=4, B=3, C=1
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 45, "B" to 35, "C" to 20),
            nbSieges = 8
        )

        assertEquals(mapOf("A" to 4, "B" to 3, "C" to 1), res.siegesAttribues)
        assertEquals(8, res.siegesAttribues.values.sum())
    }

    @Test
    fun `vote blanc exclu des suffrages exprimes`() {
        // 10 blancs + 90 pour A, 5 sièges
        // Suffrages exprimés = 90, quotient = 18, A = 90/18 = 5 sièges
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 90, null to 10),
            nbSieges = 5
        )

        assertEquals(mapOf("A" to 90), res.resultatsParListe)
        assertEquals(mapOf("A" to 5), res.siegesAttribues)
        assertTrue(res.procesVerbal.contains("Votes blancs : 10"))
        assertTrue(res.procesVerbal.contains("Suffrages exprimés : 90"))
    }

    @Test
    fun `egalite de restes departagee par le plus grand nombre de voix`() {
        // 30 votes, 4 sièges, quotient = 7
        // A 15 → 2 sièges (reste 1), B 8 → 1 (reste 1), C 7 → 1 (reste 0)
        // Sièges entiers = 4 — pas de redistribution ici, mauvais exemple.
        // Construisons : 20 votes, 3 sièges, quotient = 6
        // A 11 → 1 (reste 5), B 5 → 0 (reste 5), C 4 → 0 (reste 4)
        // Sièges entiers = 1, 2 restants. Restes A=5, B=5, C=4.
        // À égalité de reste, A (11 voix) > B (5 voix) → A puis B.
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 11, "B" to 5, "C" to 4),
            nbSieges = 3
        )

        assertEquals(mapOf("A" to 2, "B" to 1, "C" to 0), res.siegesAttribues)
    }

    @Test
    fun `egalite totale departagee de facon deterministe par id de liste`() {
        // 6 votes, 3 sièges, quotient = 2
        // A 3 → 1 siège (reste 1), B 3 → 1 siège (reste 1)
        // 1 siège restant. Restes égaux et voix égales → départage par id alpha → A.
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 3, "B" to 3),
            nbSieges = 3
        )
        assertEquals(mapOf("A" to 2, "B" to 1), res.siegesAttribues)
    }

    @Test
    fun `aucun bulletin retourne resultats vides`() {
        val res = AlgoHare.calculerResultats(emptyList(), nbSieges = 5)

        assertEquals("", res.scrutinId)
        assertEquals(emptyMap(), res.resultatsParListe)
        assertEquals(emptyMap(), res.siegesAttribues)
    }

    @Test
    fun `que des votes blancs - aucun siege attribue`() {
        val res = AlgoHare.calculerResultats(
            bulletins(null to 50),
            nbSieges = 5
        )

        assertEquals(emptyMap(), res.resultatsParListe)
        assertEquals(emptyMap(), res.siegesAttribues)
        assertTrue(res.procesVerbal.contains("Votes blancs : 50"))
        assertTrue(res.procesVerbal.contains("Suffrages exprimés : 0"))
    }

    @Test
    fun `nbSieges zero - aucun siege meme avec des voix`() {
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 10, "B" to 5),
            nbSieges = 0
        )

        assertEquals(mapOf("A" to 10, "B" to 5), res.resultatsParListe)
        assertEquals(mapOf("A" to 0, "B" to 0), res.siegesAttribues)
    }

    @Test
    fun `plus de sieges que de suffrages exprimes - sieges non pourvus`() {
        // 3 votes, 10 sièges → quotient = 0
        // Répartition par plus forts restes sur les voix brutes, 1 siège max par liste.
        // A=2, B=1 → A reçoit 1 siège (priorité voix), B reçoit 1 siège.
        // 8 sièges restent non pourvus.
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 2, "B" to 1),
            nbSieges = 10
        )

        assertEquals(mapOf("A" to 1, "B" to 1), res.siegesAttribues)
        assertTrue(res.procesVerbal.contains("Sièges attribués : 2 / 10"))
    }

    @Test
    fun `proces verbal contient les elements essentiels`() {
        val res = AlgoHare.calculerResultats(
            bulletins("A" to 45, "B" to 35, "C" to 20, null to 5),
            nbSieges = 8
        )

        val pv = res.procesVerbal
        assertTrue(pv.contains("Procès-verbal du scrutin $scrutinId"))
        assertTrue(pv.contains("Bulletins déposés : 105"))
        assertTrue(pv.contains("Votes blancs : 5"))
        assertTrue(pv.contains("Suffrages exprimés : 100"))
        assertTrue(pv.contains("Sièges à pourvoir : 8"))
        assertTrue(pv.contains("Quotient de Hare : 12"))
        assertTrue(pv.contains("A : 45 voix"))
    }
}
