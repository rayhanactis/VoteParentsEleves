package com.rayhanactis.voteparentseleves.auth

interface AuthenticationProvider {
    suspend fun authenticate(context: AuthContext): AuthResult
}
