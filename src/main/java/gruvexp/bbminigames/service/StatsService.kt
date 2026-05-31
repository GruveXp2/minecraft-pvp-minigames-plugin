package gruvexp.bbminigames.service

import gruvexp.bbminigames.database.MatchPlayerAbilityUsesTable
import gruvexp.bbminigames.database.MatchPlayersTable
import gruvexp.bbminigames.database.MatchesTable
import gruvexp.bbminigames.database.StatsDatabase
import gruvexp.bbminigames.model.stat.MatchResult
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
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

                matchResult.playerStats.forEach { (uuid, stats) ->
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
}