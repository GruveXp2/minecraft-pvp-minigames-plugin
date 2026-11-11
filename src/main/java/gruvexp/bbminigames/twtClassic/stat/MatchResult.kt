package gruvexp.bbminigames.twtClassic.stat

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.team.TeamSide
import java.time.LocalDateTime

data class MatchResult(
    val map: BotBowsMap,
    val players: Set<BotBowsPlayer>,
) {
    val startTime: LocalDateTime = LocalDateTime.now()
    var rounds = 0
    var team1Won: Boolean? = null // null = draw
    val winningTeam: TeamSide?
        get() = team1Won?.let { if (it) TeamSide.ONE else TeamSide.TWO }
    val playerStats = players.associateWith { PlayerMatchStats(it) }

    val playerStatsSorted: List<PlayerMatchStats>
        get() = playerStats.values.sortedByDescending { it.bp.team.teamSide == winningTeam } // {true..., false...}

    fun registerKill(attacker: BotBowsPlayer) {
        playerStats.getValue(attacker).addKill()
    }

    fun registerDeath(defender: BotBowsPlayer) {
        playerStats.getValue(defender).addDeath()
    }

    fun registerHit(attacker: BotBowsPlayer) { // hit others
        playerStats.getValue(attacker).addHit()
    }

    fun registerDamage(defender: BotBowsPlayer) { // got hit by others
        playerStats.getValue(defender).addDamage()
    }

    fun registerAbilitySuccess(user: BotBowsPlayer, abilityType: AbilityType) {
        playerStats.getValue(user).addAbilitySuccess(abilityType)
    }

    fun registerAbilityFail(user: BotBowsPlayer, abilityType: AbilityType) {
        playerStats.getValue(user).subtractAbilitySuccess(abilityType)
    }
}
