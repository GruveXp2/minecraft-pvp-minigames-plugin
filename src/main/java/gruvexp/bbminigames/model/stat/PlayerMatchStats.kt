package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.twtClassic.ability.AbilityType
import java.util.UUID

data class PlayerMatchStats(
    val playerId: UUID,
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