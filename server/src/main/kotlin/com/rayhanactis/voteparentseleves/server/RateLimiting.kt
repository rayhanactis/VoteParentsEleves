package com.rayhanactis.voteparentseleves.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.minutes

val RATE_LIMIT_AUTH: RateLimitName = RateLimitName("auth")

fun Application.configureRateLimiting() {
    install(RateLimit) {
        register(RATE_LIMIT_AUTH) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
        }
    }
}
