package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.auth.jwtConfigDepuisEnvironnement
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

const val REALM_ELECTEUR = "electeur"
const val REALM_ADMIN = "admin"

fun Application.configureSecurity() {
    val jwt = jwtConfigDepuisEnvironnement()
    install(Authentication) {
        jwt(REALM_ELECTEUR) {
            realm = "voteparentseleves-electeur"
            verifier(jwt.verifierElecteur())
            validate { credential ->
                if (credential.payload.audience.contains(jwt.electeurAudience))
                    JWTPrincipal(credential.payload) else null
            }
        }
        jwt(REALM_ADMIN) {
            realm = "voteparentseleves-admin"
            verifier(jwt.verifierAdmin())
            validate { credential ->
                if (credential.payload.audience.contains(jwt.adminAudience))
                    JWTPrincipal(credential.payload) else null
            }
        }
    }
}
