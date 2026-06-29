package com.rayhanactis.voteparentseleves.vote

import kotlinx.serialization.Serializable

// Indique si l'électeur authentifié a déjà voté (émargé) pour le scrutin.
// Permet à l'app de l'orienter directement vers l'écran de remerciement
// plutôt que de le laisser refaire tout le parcours pour être refusé à la fin.
@Serializable
data class StatutEmargement(val aVote: Boolean)
