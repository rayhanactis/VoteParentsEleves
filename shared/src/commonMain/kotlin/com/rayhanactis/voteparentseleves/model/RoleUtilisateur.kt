package com.rayhanactis.voteparentseleves.model

sealed class RoleUtilisateur {
    data object Admin : RoleUtilisateur()
    data object Electeur : RoleUtilisateur()
}
