package com.rayhanactis.voteparentseleves.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

// Chaque plateforme fournit son engine HTTP via expect/actual : OkHttp
// (Android), Darwin (iOS), CIO (JVM), Js (Web).
expect fun creerHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
