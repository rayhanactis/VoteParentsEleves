package com.rayhanactis.voteparentseleves.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.minutes

// 5 essais / minute / IP sur les routes de login : contre-mesure bruteforce
// (cf. ROADMAP Phase 3). La clé de quota est l'IP distante par défaut.
val RATE_LIMIT_AUTH: RateLimitName = RateLimitName("auth")

fun Application.configureRateLimiting() {
    install(RateLimit) {
        register(RATE_LIMIT_AUTH) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
        }
    }
}
