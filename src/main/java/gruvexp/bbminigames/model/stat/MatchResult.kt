package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import java.time.LocalDateTime

data class MatchResult @JvmOverloads constructor(
    val map: BotBowsMap,
    val startTime: LocalDateTime = LocalDateTime.now(),
    var rounds: Int = 0,
    var team1Won: Boolean? = null, // null = draw
    val playerStats: MutableMap<BotBowsPlayer, PlayerMatchStats> = mutableMapOf()
) {
    private fun getPlayerStats(bp: BotBowsPlayer): PlayerMatchStats {
        return playerStats.computeIfAbsent(bp) { PlayerMatchStats(bp) }
    }

    fun registerKill(attacker: BotBowsPlayer) {
        getPlayerStats(attacker).addKill()
    }

    fun registerDeath(defender: BotBowsPlayer) {
        getPlayerStats(defender).addDeath()
    }

    fun registerHit(attacker: BotBowsPlayer) { // hit others
        getPlayerStats(attacker).addHit()
    }

    fun registerDamage(defender: BotBowsPlayer) { // got hit by others
        getPlayerStats(defender).addDamage()
    }

    fun registerAbilityUse(user: BotBowsPlayer, abilityType: AbilityType) {
        getPlayerStats(user).addAbilityUse(abilityType)
    }
}
