package com.rayhanactis.voteparentseleves.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO

actual fun creerHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(CIO, config)
