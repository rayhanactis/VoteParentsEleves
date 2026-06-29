package com.rayhanactis.voteparentseleves.api

sealed class ApiResult<out T> {
    data class Succes<T>(val data: T) : ApiResult<T>()
    data class Echec(val code: Int, val message: String) : ApiResult<Nothing>()
    data class Reseau(val cause: Throwable) : ApiResult<Nothing>()

    fun messageLisible(): String = when (this) {
        is Succes -> "OK"
        is Echec -> when (code) {
            401 -> "Identifiants invalides."
            403 -> "Accès refusé."
            404 -> "Ressource introuvable."
            409 -> "Action impossible : $message"
            422 -> "Donnée invalide : $message"
            else -> "Erreur serveur (${code})."
        }
        is Reseau -> "Connexion impossible. Vérifiez le réseau."
    }
}
