package gruvexp.bbminigames.model.preset

import java.util.UUID

data class HealthPreset(
    val maxHp: Int?,
    val individualMaxHp: Map<UUID, Int>?,
    val customDamage: Map<UUID, Int>?,
)
