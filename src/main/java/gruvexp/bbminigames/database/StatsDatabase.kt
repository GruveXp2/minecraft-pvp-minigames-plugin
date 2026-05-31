package gruvexp.bbminigames.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.JDBC
import org.sqlite.SQLiteConfig
import java.io.File
import java.time.LocalDateTime

class StatsDatabase(dataFolder: File) {

    val db: Database
    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val dbFile = File(dataFolder, "stats.db")

        db =  Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
        )
        transaction(db) { // make the tables if they dont exist yet
            SchemaUtils.create(MatchesTable, MatchPlayersTable, MatchPlayerAbilityUsesTable)
        }
    }
}

object MatchesTable : Table("matches") {
    val id = integer("match_id").autoIncrement()
    val map = varchar("map", 20)
    val startTime = datetime("startTime")
    val rounds = integer("rounds").default(0)
    val team1Won = bool("team1Won").nullable()

    override val primaryKey = PrimaryKey(id)
}

object MatchPlayersTable : Table("match_players") {
    val matchId = integer("match_id").references(MatchesTable.id)
    val playerUuid = varchar("player_uuid", 36)
    val kills = integer("kills").default(0)
    val deaths = integer("deaths").default(0)
    val hits = integer("hits").default(0)
    val damage = integer("damage").default(0)

    override val primaryKey = PrimaryKey(matchId, playerUuid)
}

object MatchPlayerAbilityUsesTable : Table("match_players_ability_uses") {
    val matchId = integer("match_id").references(MatchesTable.id)
    val playerUuid = varchar("player_uuid", 36)
    val abilityType = varchar("ability_type", 36)
    val uses = integer("uses").default(0)

    override val primaryKey = PrimaryKey(matchId, playerUuid, abilityType)
}