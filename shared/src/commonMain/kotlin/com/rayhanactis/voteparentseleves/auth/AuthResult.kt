package com.rayhanactis.voteparentseleves.auth

import com.rayhanactis.voteparentseleves.model.RoleUtilisateur

sealed class AuthResult {
    data class Success(val principalId: String, val role: RoleUtilisateur) : AuthResult()
    data class Failure(val raison: String) : AuthResult()
}
