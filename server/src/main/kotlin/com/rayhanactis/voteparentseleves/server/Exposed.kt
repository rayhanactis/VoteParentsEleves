package com.rayhanactis.voteparentseleves.server

import com.rayhanactis.voteparentseleves.server.db.AdminsTable
import com.rayhanactis.voteparentseleves.server.db.BulletinsTable
import com.rayhanactis.voteparentseleves.server.db.CandidatsTable
import com.rayhanactis.voteparentseleves.server.db.ElecteursTable
import com.rayhanactis.voteparentseleves.server.db.EmargementsTable
import com.rayhanactis.voteparentseleves.server.db.ListesCandidatesTable
import com.rayhanactis.voteparentseleves.server.db.ParametresEcoleTable
import com.rayhanactis.voteparentseleves.server.db.ScrutinsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import java.util.concurrent.atomic.AtomicReference

fun Application.configureExposed() {
    val dbFile = File("votes.db")
    DbHolder.initialiser(dbFile)

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            ElecteursTable,
            AdminsTable,
            ScrutinsTable,
            ListesCandidatesTable,
            CandidatsTable,
            BulletinsTable,
            EmargementsTable,
            ParametresEcoleTable
        )
    }

    log.info(
        "SQLite prêt : ${dbFile.absolutePath} (journal=WAL, busy_timeout=5s, " +
            "FK=ON, synchronous=NORMAL, pool=5)"
    )
    appliquerSeedSiDemande()
}

object DbHolder {
    private val dbFileRef = AtomicReference<File>()
    private val dataSourceRef = AtomicReference<HikariDataSource>()

    fun fichierCourant(): File = dbFileRef.get()
    fun dataSource(): HikariDataSource = dataSourceRef.get()

    fun initialiser(dbFile: File) {
        dbFileRef.set(dbFile)
        val ds = creerPoolSqlite(dbFile)
        dataSourceRef.set(ds)
        Database.connect(ds)
    }

    fun remplacerPar(fichierSource: File) {
        val cible = dbFileRef.get()
        dataSourceRef.get()?.close()
        fichierSource.copyTo(cible, overwrite = true)
        val ds = creerPoolSqlite(cible)
        dataSourceRef.set(ds)
        Database.connect(ds)
    }
}

private fun creerPoolSqlite(dbFile: File): HikariDataSource {
    val sqliteDataSource = SQLiteDataSource(
        SQLiteConfig().apply {
            setJournalMode(SQLiteConfig.JournalMode.WAL)
            setBusyTimeout(5_000)
            enforceForeignKeys(true)
            setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
            setTempStore(SQLiteConfig.TempStore.MEMORY)
        }
    ).apply {
        url = "jdbc:sqlite:${dbFile.absolutePath}"
    }

    val hikariConfig = HikariConfig().apply {
        dataSource = sqliteDataSource
        poolName = "voteparentseleves-sqlite"
        maximumPoolSize = 5
        minimumIdle = 1
        connectionTestQuery = "SELECT 1"
    }
    return HikariDataSource(hikariConfig)
}
