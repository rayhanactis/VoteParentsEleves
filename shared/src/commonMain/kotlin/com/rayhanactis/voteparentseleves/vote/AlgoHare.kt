package com.rayhanactis.voteparentseleves.vote

import com.rayhanactis.voteparentseleves.model.Bulletin
import com.rayhanactis.voteparentseleves.model.ResultatScrutin

object AlgoHare : AlgorithmeVote {

    override fun calculerResultats(
        bulletins: List<Bulletin>,
        nbSieges: Int
    ): ResultatScrutin {
        val scrutinId = bulletins.firstOrNull()?.scrutinId.orEmpty()

        val (bulletinsExprimes, bulletinsBlancs) = bulletins
            .partition { it.listeCandidateId != null }

        val resultatsParListe: Map<String, Int> = bulletinsExprimes
            .groupingBy { it.listeCandidateId!! }
            .eachCount()

        val siegesAttribues: Map<String, Int> = when {
            nbSieges <= 0 || resultatsParListe.isEmpty() ->
                resultatsParListe.mapValues { 0 }
            else -> attribuerSieges(resultatsParListe, nbSieges)
        }

        val pv = genererProcesVerbal(
            scrutinId = scrutinId,
            totalBulletins = bulletins.size,
            votesBlancs = bulletinsBlancs.size,
            nbSieges = nbSieges,
            resultatsParListe = resultatsParListe,
            siegesAttribues = siegesAttribues
        )

        return ResultatScrutin(
            scrutinId = scrutinId,
            resultatsParListe = resultatsParListe,
            siegesAttribues = siegesAttribues,
            procesVerbal = pv
        )
    }

    private fun attribuerSieges(
        resultatsParListe: Map<String, Int>,
        nbSieges: Int
    ): Map<String, Int> {
        val totalExprimes = resultatsParListe.values.sum()
        val quotient = totalExprimes / nbSieges

        // Cas dégénéré : moins de suffrages exprimés que de sièges.
        // Répartition par plus forts restes en utilisant directement les voix.
        if (quotient == 0) {
            return repartirAuPlusFortReste(
                siegesInitiaux = resultatsParListe.mapValues { 0 },
                restes = resultatsParListe,
                votesBruts = resultatsParListe,
                siegesAPourvoir = nbSieges
            )
        }

        val siegesEntiers: Map<String, Int> = resultatsParListe
            .mapValues { (_, voix) -> voix / quotient }

        val restes: Map<String, Int> = resultatsParListe
            .mapValues { (liste, voix) -> voix - siegesEntiers.getValue(liste) * quotient }

        val siegesRestants = nbSieges - siegesEntiers.values.sum()

        return if (siegesRestants <= 0) siegesEntiers
        else repartirAuPlusFortReste(
            siegesInitiaux = siegesEntiers,
            restes = restes,
            votesBruts = resultatsParListe,
            siegesAPourvoir = siegesRestants
        )
    }

    // Plus fort reste : on trie par reste décroissant, puis par voix totales
    // décroissantes (départage légal), puis par id de liste pour déterminisme.
    private fun repartirAuPlusFortReste(
        siegesInitiaux: Map<String, Int>,
        restes: Map<String, Int>,
        votesBruts: Map<String, Int>,
        siegesAPourvoir: Int
    ): Map<String, Int> {
        val gagnants: Set<String> = restes.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenByDescending { votesBruts.getValue(it.key) }
                    .thenBy { it.key }
            )
            .take(siegesAPourvoir)
            .map { it.key }
            .toSet()

        return siegesInitiaux.mapValues { (liste, sieges) ->
            sieges + if (liste in gagnants) 1 else 0
        }
    }

    private fun genererProcesVerbal(
        scrutinId: String,
        totalBulletins: Int,
        votesBlancs: Int,
        nbSieges: Int,
        resultatsParListe: Map<String, Int>,
        siegesAttribues: Map<String, Int>
    ): String {
        val exprimes = totalBulletins - votesBlancs
        val quotient = if (nbSieges > 0 && exprimes > 0) exprimes / nbSieges else 0
        val siegesPourvus = siegesAttribues.values.sum()

        val lignesListes = resultatsParListe.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
            )
            .joinToString(separator = "\n") { (liste, voix) ->
                val sieges = siegesAttribues[liste] ?: 0
                "  - $liste : $voix voix → $sieges siège(s)"
            }
            .ifEmpty { "  (aucune liste n'a recueilli de suffrage)" }

        return buildString {
            appendLine("Procès-verbal du scrutin $scrutinId")
            appendLine("Bulletins déposés : $totalBulletins")
            appendLine("Votes blancs : $votesBlancs")
            appendLine("Suffrages exprimés : $exprimes")
            appendLine("Sièges à pourvoir : $nbSieges")
            appendLine("Quotient de Hare : $quotient")
            appendLine("Répartition (méthode du plus fort reste) :")
            appendLine(lignesListes)
            append("Sièges attribués : $siegesPourvus / $nbSieges")
        }
    }
}
