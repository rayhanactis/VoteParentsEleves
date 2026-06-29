package com.rayhanactis.voteparentseleves.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual fun creerHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Js, config)
