package dev.gruve.minigames.botbows.preset

import dev.gruve.minigames.botbows.map.BotBowsMap
import dev.gruve.minigames.botbows.hazard.HazardChance
import dev.gruve.minigames.botbows.hazard.HazardType
import org.bukkit.Material
import java.util.UUID

data class BattlePreset(
    val name: String,
    val icon: Material,
    val map: BotBowsMap,
    val team1: Set<UUID>,
    val team2: Set<UUID>,
    val health: HealthPreset,
    val winCondition: WinConditionPreset,
    val hazards: Map<HazardType, HazardChance>,
    val abilities: AbilityPreset,
)
