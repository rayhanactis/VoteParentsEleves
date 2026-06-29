package com.rayhanactis.voteparentseleves.admin

import com.rayhanactis.voteparentseleves.server.SeedDemo
import com.rayhanactis.voteparentseleves.server.configureAutoHeadResponse
import com.rayhanactis.voteparentseleves.server.configureExposed
import com.rayhanactis.voteparentseleves.server.configureHttp
import com.rayhanactis.voteparentseleves.server.configureMonitoring
import com.rayhanactis.voteparentseleves.server.configureResources
import com.rayhanactis.voteparentseleves.server.configureRouting
import com.rayhanactis.voteparentseleves.server.configureSecurity
import com.rayhanactis.voteparentseleves.server.configureSerialization
import com.rayhanactis.voteparentseleves.server.configureStatusPages
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

// Démarre Ktor in-process (même JVM que la fenêtre Compose Desktop).
// Sert le réseau local de l'école sur 0.0.0.0:port.
// Pas de yaml chargé — jwtConfigDepuisEnvironnement() utilise ses
// valeurs par défaut. Le seed démo est appliqué inconditionnellement
// après le schéma pour que l'admin puisse se logger dès le 1er run.
fun demarrerServeurEmbarque(
    port: Int = 8080
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureHttp()
        configureMonitoring()
        configureSerialization()
        configureSecurity()
        configureResources()
        configureStatusPages()
        configureAutoHeadResponse()
        configureExposed()
        configureRouting()
        // Idempotent : no-op si le scrutin démo est déjà présent.
        SeedDemo.executer()
    }
    server.start(wait = false)
    return server
}
