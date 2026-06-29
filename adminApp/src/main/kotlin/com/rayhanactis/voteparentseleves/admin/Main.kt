package com.rayhanactis.voteparentseleves.admin

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rayhanactis.voteparentseleves.admin.ui.AdminApp

fun main() {
    val serveur = demarrerServeurEmbarque(port = 8080)
    Runtime.getRuntime().addShutdownHook(
        Thread { serveur.stop(gracePeriodMillis = 0, timeoutMillis = 1_000) }
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "VoteParentsEleves — Administration",
            state = rememberWindowState(size = DpSize(1280.dp, 800.dp))
        ) {
            AdminApp()
        }
    }
}
