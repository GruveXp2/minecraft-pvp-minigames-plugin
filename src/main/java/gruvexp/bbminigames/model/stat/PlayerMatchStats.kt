package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityEffect
import gruvexp.bbminigames.twtClassic.ability.AbilityType

data class PlayerMatchStats(
    val bp: BotBowsPlayer,
    var kills: Int = 0,
    var deaths: Int = 0,
    var hits: Int = 0,
    var damage: Int = 0,
    val abilitySuccesses: MutableMap<AbilityType, Int> = mutableMapOf()
)  {
    val crossbowHits: Int
        get() = hits - abilitySuccesses
            .filter { it.key.effect == AbilityEffect.DAMAGE }
            .map { it.value }
            .sum()

    fun addKill() { kills++ }
    fun addDeath() { deaths++ }
    fun addHit() { hits++ }
    fun addDamage() { damage++ }
    fun addAbilitySuccess(abilityType: AbilityType) {
        abilitySuccesses[abilityType] = abilitySuccesses.getOrDefault(abilityType, 0) + 1
    }
    fun subtractAbilitySuccess(abilityType: AbilityType) {
        abilitySuccesses[abilityType] = abilitySuccesses.getOrDefault(abilityType, 0) - 1
    }
}