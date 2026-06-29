package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.auth.JwtConfig
import com.rayhanactis.voteparentseleves.server.auth.JwtIssuer
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.testing.ApplicationTestBuilder

val testJwtConfig = JwtConfig(
    secret = "test-secret-suffisamment-long-pour-hmac",
    issuer = "test-vpe",
    electeurAudience = "electeur",
    adminAudience = "admin",
    electeurTtlSeconds = 3_600L,
    adminTtlSeconds = 3_600L
)

fun ApplicationTestBuilder.installAuthTest(jwt: JwtConfig = testJwtConfig) {
    application {
        install(Authentication) {
            jwt(REALM_ELECTEUR) {
                verifier(jwt.verifierElecteur())
                validate { credential ->
                    if (credential.payload.audience.contains(jwt.electeurAudience))
                        JWTPrincipal(credential.payload) else null
                }
            }
            jwt(REALM_ADMIN) {
                verifier(jwt.verifierAdmin())
                validate { credential ->
                    if (credential.payload.audience.contains(jwt.adminAudience))
                        JWTPrincipal(credential.payload) else null
                }
            }
        }
    }
}

fun tokenElecteur(electeurId: String, scrutinId: String, jwt: JwtConfig = testJwtConfig): String =
    JwtIssuer.pourElecteur(jwt, electeurId, scrutinId)

fun tokenAdmin(adminId: String = "admin-1", jwt: JwtConfig = testJwtConfig): String =
    JwtIssuer.pourAdmin(jwt, adminId)

fun HttpRequestBuilder.bearer(token: String) {
    header(HttpHeaders.Authorization, "Bearer $token")
}
