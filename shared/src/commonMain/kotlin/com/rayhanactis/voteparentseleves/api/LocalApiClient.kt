package com.rayhanactis.voteparentseleves.api

import androidx.compose.runtime.staticCompositionLocalOf

val LocalApiClient = staticCompositionLocalOf<ApiClient> {
    error("ApiClient non fourni — wrap dans CompositionLocalProvider(LocalApiClient provides …)")
}
