package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.team.TeamSide
import java.time.LocalDateTime

data class MatchResult(
    val map: BotBowsMap,
    val startTime: LocalDateTime = LocalDateTime.now(),
    var rounds: Int = 0,
    var team1Won: Boolean? = null, // null = draw
    val winningTeam: TeamSide? = team1Won?.let { if (it) TeamSide.ONE else TeamSide.TWO },
    val playerStats: MutableMap<BotBowsPlayer, PlayerMatchStats> = mutableMapOf()
) {
    val playerStatsSorted: List<PlayerMatchStats>
        get() = playerStats.values.sortedBy { it.bp.team.teamSide }

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

    fun registerAbilitySuccess(user: BotBowsPlayer, abilityType: AbilityType) {
        getPlayerStats(user).addAbilitySuccess(abilityType)
    }

    fun registerAbilityFail(user: BotBowsPlayer, abilityType: AbilityType) {
        getPlayerStats(user).subtractAbilitySuccess(abilityType)
    }
}
