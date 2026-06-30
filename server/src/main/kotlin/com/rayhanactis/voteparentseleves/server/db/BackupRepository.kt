package com.rayhanactis.voteparentseleves.server.db

import com.rayhanactis.voteparentseleves.server.DbHolder
import java.io.File

sealed class ResultatSauvegarde {
    data class Succes(val fichier: File) : ResultatSauvegarde()
    data class Echec(val raison: String) : ResultatSauvegarde()
}

sealed class ResultatRestauration {
    data object Succes : ResultatRestauration()
    data class Echec(val raison: String) : ResultatRestauration()
}

object BackupRepository {

    fun sauvegarder(destination: File): ResultatSauvegarde = runCatching {
        val cheminEchappe = destination.absolutePath.replace("'", "''")
        DbHolder.dataSource().connection.use { connexion ->
            connexion.createStatement().use { it.execute("VACUUM INTO '$cheminEchappe'") }
        }
        ResultatSauvegarde.Succes(destination)
    }.getOrElse { ResultatSauvegarde.Echec(it.message ?: "Échec de la sauvegarde") }

    fun restaurer(source: File): ResultatRestauration {
        if (!estUneBaseSqliteValide(source)) {
            return ResultatRestauration.Echec("Le fichier sélectionné n'est pas une base SQLite valide")
        }
        return runCatching {
            DbHolder.remplacerPar(source)
            ResultatRestauration.Succes
        }.getOrElse { ResultatRestauration.Echec(it.message ?: "Échec de la restauration") }
    }

    fun cheminBaseCourante(): File = DbHolder.fichierCourant()

    private fun estUneBaseSqliteValide(fichier: File): Boolean {
        if (!fichier.exists() || fichier.length() < 16) return false
        val entete = ByteArray(16)
        fichier.inputStream().use { it.read(entete) }
        return entete.decodeToString(0, 15) == "SQLite format 3"
    }
}
