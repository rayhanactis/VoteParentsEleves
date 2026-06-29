package com.rayhanactis.voteparentseleves.ui.mock

// Configuration côté client purement présentationnelle (l'API renvoie
// désormais les vraies listes et candidats). Les "professions de foi"
// ne sont pas (encore) stockées en base : on les conserve client-side
// indexées par listeId et complètent les données API à l'affichage.
object MockData {
    const val SCRUTIN_ID = "scr-demo-2026"
    const val ECOLE_NOM = "École Jean-Moulin"

    val descriptions: Map<String, String> = mapOf(
        "lst-parents-actifs" to "Nous voulons une école plus ouverte : davantage d'activités péri-scolaires, un dialogue régulier avec les enseignants et plus de sorties culturelles.",
        "lst-pour-nos-enfants" to "Notre priorité, c'est le quotidien des enfants : qualité de la cantine, bâtiments rénovés et sécurité aux abords de l'école.",
        "lst-engagement" to "Pour une école qui éduque à la citoyenneté, lutte contre les inégalités et met en avant la diversité de toutes les familles."
    )
}
