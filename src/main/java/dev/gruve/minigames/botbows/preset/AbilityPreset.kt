package dev.gruve.minigames.botbows.preset

import dev.gruve.minigames.botbows.ability.AbilityType
import java.util.UUID

data class AbilityPreset(
    val maxAbilities: Int?,
    val individualMaxAbilities: Map<UUID, Int>?,
    val cooldownMultiplier: Float?,
    val individualCooldownMultiplier: Map<UUID, Float>?,
    val bannedAbilities: Set<AbilityType>?,
    val isUniqueMode: Boolean,
    val isRandomizedMode: Boolean,
)
