package gruvexp.bbminigames.model.preset

import gruvexp.bbminigames.twtClassic.ability.AbilityType
import java.util.UUID

@JvmRecord
data class AbilityPreset(
    val maxAbilities: Int?,
    val individualMaxAbilities: Map<UUID, Int>?,
    val cooldownMultiplier: Float?,
    val individualCooldownMultiplier: Map<UUID, Float>?,
    val bannedAbilities: Set<AbilityType>?,
)
