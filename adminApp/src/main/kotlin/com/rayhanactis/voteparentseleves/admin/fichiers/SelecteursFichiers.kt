package com.rayhanactis.voteparentseleves.admin.fichiers

import java.awt.FileDialog
import java.io.File

fun choisirFichierAOuvrir(titre: String, extension: String? = null): File? {
    val dialog = FileDialog(null as java.awt.Frame?, titre, FileDialog.LOAD)
    if (extension != null) dialog.file = "*.$extension"
    dialog.isVisible = true
    val nom = dialog.file ?: return null
    return File(dialog.directory, nom)
}

fun choisirDestinationFichier(titre: String, nomParDefaut: String): File? {
    val dialog = FileDialog(null as java.awt.Frame?, titre, FileDialog.SAVE)
    dialog.file = nomParDefaut
    dialog.isVisible = true
    val nom = dialog.file ?: return null
    return File(dialog.directory, nom)
}
