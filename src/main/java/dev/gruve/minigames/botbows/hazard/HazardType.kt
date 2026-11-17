package dev.gruve.minigames.botbows.hazard

import dev.gruve.minigames.botbows.hazard.hazards.EarthquakeHazard
import dev.gruve.minigames.botbows.hazard.hazards.GhostHazard
import dev.gruve.minigames.botbows.hazard.hazards.StormHazard
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class HazardType(
    val displayName: String,
    val defaultChance: HazardChance,
    val menuFillItem: Material,
    val textColor: NamedTextColor,
    private val create: () -> Hazard
) {
    STORM("Storm",
        HazardChance.TEN,
        Material.CYAN_STAINED_GLASS_PANE,
        NamedTextColor.AQUA,
        ::StormHazard
    ),
    EARTHQUAKE(
        "Earthquake",
        HazardChance.FIVE,
        Material.BROWN_STAINED_GLASS_PANE,
        NamedTextColor.GOLD,
        ::EarthquakeHazard
    ),
    GHOST(
        "Ghost",
        HazardChance.DISABLED,
        Material.PURPLE_STAINED_GLASS_PANE,
        NamedTextColor.LIGHT_PURPLE,
        ::GhostHazard,
    );

    fun createHazard(): Hazard = create()
}
