package com.rayhanactis.voteparentseleves

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform