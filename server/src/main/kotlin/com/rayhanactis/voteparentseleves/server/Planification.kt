package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.db.AdminRepository
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Ticker périodique qui recale les statuts des scrutins programmés (ouverture et
 * fermeture automatiques aux dates prévues), en complément de la réconciliation
 * déjà faite à chaque lecture des scrutins. Le `launch` est lié au scope de
 * l'Application : il s'annule proprement à l'arrêt du serveur.
 */
fun Application.configurePlanification(intervalleMs: Long = 30_000L) {
    launch {
        while (isActive) {
            delay(intervalleMs)
            runCatching { AdminRepository.reconcilierStatuts() }
                .onFailure { log.warn("Réconciliation périodique des statuts échouée: ${it.message}") }
        }
    }
}
