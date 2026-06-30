package com.rayhanactis.voteparentseleves.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun creerHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
