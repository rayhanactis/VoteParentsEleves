package com.rayhanactis.voteparentseleves.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.response.*

// Audit (ROADMAP Phase 3) : CallLogging n'est volontairement pas installé.
// Ktor ne loggue jamais les bodies de requête par défaut, mais on évite même
// la ligne de log par appel pour ne pas avoir à auditer un filtre sur
// /auth/* plus tard si CallLogging était activé sans précaution.
fun Application.configureMonitoring() {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
}