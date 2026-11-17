package dev.gruve.minigames.botbows.stat

import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.AbilityEffect
import dev.gruve.minigames.botbows.ability.AbilityType

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