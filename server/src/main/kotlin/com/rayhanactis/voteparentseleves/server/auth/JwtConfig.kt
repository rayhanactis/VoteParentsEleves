package com.rayhanactis.voteparentseleves.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import java.util.Date

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val electeurAudience: String,
    val adminAudience: String,
    val electeurTtlSeconds: Long,
    val adminTtlSeconds: Long
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun verifierElecteur(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(electeurAudience)
        .build()

    fun verifierAdmin(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(adminAudience)
        .build()
}

object JwtIssuer {
    const val CLAIM_SCRUTIN = "scrutinId"

    fun pourElecteur(
        config: JwtConfig,
        electeurId: String,
        scrutinId: String,
        expiration: Date = Date(System.currentTimeMillis() + config.electeurTtlSeconds * 1000)
    ): String = JWT.create()
        .withIssuer(config.issuer)
        .withAudience(config.electeurAudience)
        .withSubject(electeurId)
        .withClaim(CLAIM_SCRUTIN, scrutinId)
        .withExpiresAt(expiration)
        .sign(config.algorithm)

    fun pourAdmin(
        config: JwtConfig,
        adminId: String,
        expiration: Date = Date(System.currentTimeMillis() + config.adminTtlSeconds * 1000)
    ): String = JWT.create()
        .withIssuer(config.issuer)
        .withAudience(config.adminAudience)
        .withSubject(adminId)
        .withExpiresAt(expiration)
        .sign(config.algorithm)
}

fun Application.jwtConfigDepuisEnvironnement(): JwtConfig {
    val cfg = environment.config
    val secretYaml = cfg.tryGetString("jwt.secret")?.takeIf { it.isNotBlank() }
    return JwtConfig(
        secret = secretYaml ?: SecretStore.chargerOuCreer(cheminSecretParDefaut()),
        issuer = cfg.tryGetString("jwt.issuer") ?: "voteparentseleves",
        electeurAudience = cfg.tryGetString("jwt.audience.electeur") ?: "electeur",
        adminAudience = cfg.tryGetString("jwt.audience.admin") ?: "admin",
        electeurTtlSeconds = cfg.tryGetString("jwt.ttl.electeurSec")?.toLong() ?: 86_400L,
        adminTtlSeconds = cfg.tryGetString("jwt.ttl.adminSec")?.toLong() ?: 3_600L
    )
}
