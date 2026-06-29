package com.rayhanactis.voteparentseleves.admin.csv

import com.rayhanactis.voteparentseleves.admin.LigneElecteurBrute

sealed class ResultatParsingCsv {
    data class Succes(val lignes: List<LigneElecteurBrute>) : ResultatParsingCsv()
    data class Echec(val raison: String) : ResultatParsingCsv()
}

// Format attendu : une ligne par parent "Nom,Prénom" ou "Nom;Prénom".
// Une éventuelle ligne d'en-tête ("Nom,Prenom") est détectée et ignorée.
fun parserCsvElecteurs(contenu: String): ResultatParsingCsv {
    val lignesBrutes = contenu.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lignesBrutes.isEmpty()) return ResultatParsingCsv.Echec("Le fichier est vide.")

    val separateur = if (lignesBrutes.first().contains(';')) ';' else ','
    val lignes = lignesBrutes.mapIndexedNotNull { index, ligne ->
        val colonnes = ligne.split(separateur).map { it.trim() }
        if (colonnes.size < 2 || colonnes[0].isBlank() || colonnes[1].isBlank()) return@mapIndexedNotNull null
        if (index == 0 && colonnes[0].equals("nom", ignoreCase = true)) return@mapIndexedNotNull null
        LigneElecteurBrute(nom = colonnes[0], prenom = colonnes[1])
    }
    return if (lignes.isEmpty()) {
        ResultatParsingCsv.Echec("Aucune ligne valide trouvée (format attendu : Nom,Prénom).")
    } else {
        ResultatParsingCsv.Succes(lignes)
    }
}
