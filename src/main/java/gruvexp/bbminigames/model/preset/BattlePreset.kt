package gruvexp.bbminigames.model.preset

import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.hazard.HazardChance
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import org.bukkit.Material
import java.util.UUID

@JvmRecord
data class BattlePreset(
    val name: String,
    val icon: Material,
    val map: BotBowsMap,
    val team1: Set<UUID>,
    val team2: Set<UUID>,
    val health: HealthSettings,
    val winCondition: WinConditionSettings,
    val hazards: Map<HazardType, HazardChance>,
    val abilities: AbilitySettings,
)
