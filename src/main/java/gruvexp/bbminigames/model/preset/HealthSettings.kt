package gruvexp.bbminigames.model.preset

import java.util.UUID

@JvmRecord
data class HealthSettings(
    val maxHp: Int?,
    val individualMaxHp: Map<UUID, Int>?,
    val customDamage: Map<UUID, Int>?,
)
