package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.db.BackupRepository
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.ResultatRestauration
import com.rayhanactis.voteparentseleves.server.db.ResultatSauvegarde
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupRepositoryTest {

    private val dossierTemp = File.createTempFile("vpe-backup-test", "").let {
        it.delete()
        it.mkdirs()
        it
    }

    @AfterTest
    fun nettoyage() {
        dossierTemp.deleteRecursively()
    }

    @Test
    fun `sauvegarde puis restauration preserve les donnees`() {
        val dbFichier = File(dossierTemp, "votes.db")
        DbHolder.initialiser(dbFichier)
        transaction { SchemaUtils.create(ElecteursTable) }
        transaction {
            ElecteursTable.insert {
                it[id] = "el-1"; it[nom] = "Dupont"; it[prenom] = "Alice"; it[ecoleId] = "ecole-1"
            }
        }

        val backupFichier = File(dossierTemp, "backup.db")
        val resultatBackup = BackupRepository.sauvegarder(backupFichier)
        assertTrue(resultatBackup is ResultatSauvegarde.Succes)
        assertTrue(backupFichier.exists() && backupFichier.length() > 0)

        transaction {
            ElecteursTable.insert {
                it[id] = "el-2"; it[nom] = "Martin"; it[prenom] = "Bob"; it[ecoleId] = "ecole-1"
            }
        }
        assertEquals(2, transaction { ElecteursTable.selectAll().count() })

        val resultatRestauration = BackupRepository.restaurer(backupFichier)
        assertEquals(ResultatRestauration.Succes, resultatRestauration)
        assertEquals(1, transaction { ElecteursTable.selectAll().count() })
    }

    @Test
    fun `restaurer un fichier qui n'est pas une base sqlite echoue proprement`() {
        val dbFichier = File(dossierTemp, "votes2.db")
        DbHolder.initialiser(dbFichier)
        transaction { SchemaUtils.create(ElecteursTable) }

        val fauxFichier = File(dossierTemp, "pas-une-base.txt").apply { writeText("bonjour") }
        val resultat = BackupRepository.restaurer(fauxFichier)
        assertTrue(resultat is ResultatRestauration.Echec)
    }
}
