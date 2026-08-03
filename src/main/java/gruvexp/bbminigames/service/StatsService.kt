package gruvexp.bbminigames.service

import gruvexp.bbminigames.database.MatchPlayerAbilityUsesTable
import gruvexp.bbminigames.database.MatchPlayersTable
import gruvexp.bbminigames.database.MatchesTable
import gruvexp.bbminigames.database.StatsDatabase
import gruvexp.bbminigames.model.stat.MatchResult
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class StatsService(
    private val plugin: JavaPlugin,
    private val statsDatabase: StatsDatabase
) {
    fun saveMatchResult(matchResult: MatchResult) {
        // database calls can take noticeable time, so shouldnt run on main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            transaction(statsDatabase.db) {
                val matchId = MatchesTable.insert {
                    it[map] = matchResult.map.name
                    it[startTime] = matchResult.startTime
                    it[rounds] = matchResult.rounds
                    it[team1Won] = matchResult.team1Won
                } get MatchesTable.id

                matchResult.playerStats.forEach { (bp, stats) ->
                    MatchPlayersTable.insert {
                        it[this.matchId] = matchId
                        it[playerUuid] = uuid.toString()
                        it[kills] = stats.kills
                        it[deaths] = stats.deaths
                        it[hits] = stats.hits
                        it[damage] = stats.damage
                    }
                    stats.abilityUses.forEach { (type, amount) ->
                        MatchPlayerAbilityUsesTable.insert {
                            it[this.matchId] = matchId
                            it[playerUuid] = uuid.toString()
                            it[abilityType] = type.name
                            it[uses] = amount
                        }
                    }
                }
            }
            BotBows.debugMessage("saved to db!")
        })
    }
    fun printOutInfo() { // chat code to just quickly check if it works
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            transaction(statsDatabase.db) {
                BotBows.debugMessage("--- Dumper databasedata ---")

                // 1. Print ut alt fra MatchesTable
                BotBows.debugMessage("=== KAMPER ===")
                MatchesTable.selectAll().forEach { row ->
                    BotBows.debugMessage(
                        "ID: ${row[MatchesTable.id]} | " +
                                "Kart: ${row[MatchesTable.map]} | " +
                                "Start: ${row[MatchesTable.startTime]} | " +
                                "Runder: ${row[MatchesTable.rounds]} | " +
                                "Team 1 Vant: ${row[MatchesTable.team1Won]}"
                    )
                }

                // 2. Print ut alt fra MatchPlayersTable
                BotBows.debugMessage("=== SPILLERSTATISTIKK ===")
                MatchPlayersTable.selectAll().forEach { row ->
                    BotBows.debugMessage(
                        "Kamp-ID: ${row[MatchPlayersTable.matchId]} | " +
                                "UUID: ${row[MatchPlayersTable.playerUuid]} | " +
                                "Kills: ${row[MatchPlayersTable.kills]} | " +
                                "Deaths: ${row[MatchPlayersTable.deaths]} | " +
                                "Hits: ${row[MatchPlayersTable.hits]} | " +
                                "Damage: ${row[MatchPlayersTable.damage]}"
                    )
                }
                BotBows.debugMessage("---------------------------")
            }
        })
    }

    fun getLastMatchStats(playerUuid: UUID): LastMatchStatsResult? {
        return transaction(statsDatabase.db) {
            val lastMatchId = MatchPlayersTable
                .select(MatchPlayersTable.matchId)
                .where { MatchPlayersTable.playerUuid eq playerUuid.toString() } // must use eq instead of ==
                .orderBy(MatchPlayersTable.matchId to SortOrder.DESC)
                .limit(1)
                .singleOrNull()?.get(MatchPlayersTable.matchId) ?: return@transaction null

            val matchRow = MatchesTable
                .selectAll()
                .where { MatchesTable.id eq lastMatchId }
                .single()

            val botBowsMap = BotBowsMap.valueOf(matchRow[MatchesTable.map])

            val allPlayersInMatch = MatchPlayersTable
                .selectAll()
                .where { MatchPlayersTable.matchId eq lastMatchId }
                .toList()

            val ownStats = allPlayersInMatch.first { it[MatchPlayersTable.playerUuid] == playerUuid.toString() }

            val topKiller = allPlayersInMatch.maxBy { it[MatchPlayersTable.kills] }
            val topDeather = allPlayersInMatch.maxBy { it[MatchPlayersTable.deaths] }
            val topDamager = allPlayersInMatch.maxBy { it[MatchPlayersTable.damage] }

            val topAbilities = MatchPlayerAbilityUsesTable
                .select(MatchPlayerAbilityUsesTable.abilityType, MatchPlayerAbilityUsesTable.uses)
                .where {
                    (MatchPlayerAbilityUsesTable.matchId eq lastMatchId) and
                            (MatchPlayerAbilityUsesTable.playerUuid eq playerUuid.toString())
                }
                .orderBy(MatchPlayerAbilityUsesTable.uses to SortOrder.DESC)
                .limit(3)
                .map {
                    AbilityType.valueOf(it[MatchPlayerAbilityUsesTable.abilityType]) to it[MatchPlayerAbilityUsesTable.uses]
                }

            LastMatchStatsResult(
                map = botBowsMap,
                playerKills = ownStats[MatchPlayersTable.kills],
                playerDeaths = ownStats[MatchPlayersTable.deaths],
                playerHits = ownStats[MatchPlayersTable.hits],
                playerDamage = ownStats[MatchPlayersTable.damage],
                mostKillsPlayer = UUID.fromString(topKiller[MatchPlayersTable.playerUuid]),
                mostKillsCount = topKiller[MatchPlayersTable.kills],
                mostDeathsPlayer = UUID.fromString(topDeather[MatchPlayersTable.playerUuid]),
                mostDeathsCount = topDeather[MatchPlayersTable.deaths],
                mostDamagePlayer = UUID.fromString(topDamager[MatchPlayersTable.playerUuid]),
                mostDamageCount = topDamager[MatchPlayersTable.damage],
                topAbilities = topAbilities
            )
        }
    }
}

data class LastMatchStatsResult(
    val map: BotBowsMap, //TODO: gjør at når man trykker på statten som printes ut, akkurat på mappnavnet, går man i spectimode og tpes te mappet, hvor man kan gå rundt og se. man kan og gå i adventuremode. når man er ferdig går man i specti og flyr ut av mappet og det trigger at man tper tebake
    val playerKills: Int,
    val playerDeaths: Int,
    val playerHits: Int,
    val playerDamage: Int,
    val mostKillsPlayer: UUID,
    val mostKillsCount: Int,
    val mostDeathsPlayer: UUID,
    val mostDeathsCount: Int,
    val mostDamagePlayer: UUID,
    val mostDamageCount: Int,
    val topAbilities: List<Pair<AbilityType, Int>> // top 3 abilities used
)