package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType

data class PlayerMatchStats(
    val bp: BotBowsPlayer,
    var kills: Int = 0,
    var deaths: Int = 0,
    var hits: Int = 0,
    var damage: Int = 0,
    val abilityUses: MutableMap<AbilityType, Int> = mutableMapOf()
)  {
    fun addKill() { kills++ }
    fun addDeath() { deaths++ }
    fun addHit() { hits++ }
    fun addDamage() { damage++ }
    fun addAbilityUse(abilityType: AbilityType) {
        abilityUses[abilityType] = abilityUses.getOrDefault(abilityType, 0) + 1
    }
}