package gruvexp.bbminigames.twtClassic.map

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class BotBowsMap(
    val mapType: MapType,
    val allowedHazards: Set<HazardType>,
    val team1: BotBowsTeam,
    val team2: BotBowsTeam,
    private val item: ItemStack,
    val viewingLocation: Location,
) { // TODO: gjør at 2 lobbies ikke kan ha samme map, pga nå er jo mapsane enums, og det ville bøgga te playersa i teamsa
    RANDOM(
        MapType.CLASSIC, setOf(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
        BotBowsTeam.BLAUD, BotBowsTeam.SAUCE,
        Menu.makeItem(
            Material.TARGET, Component.text("Random Map", NamedTextColor.WHITE),
            Component.text("Randomly picks one of the classic maps")
        ),
        Main.WORLD_SPAWN_LOBBY,
    ),

    CLASSIC_ARENA(
        MapType.CLASSIC, setOf(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
        BotBowsTeam.BLAUD, BotBowsTeam.SAUCE,
        Menu.makeItem(
            Material.SLIME_BALL, Component.text("Classic Arena", NamedTextColor.GRAY),
            Component.text("Blaud", NamedTextColor.BLUE)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Sauce", NamedTextColor.RED)),
            Component.text("A flat arena with modern royal style"),
            Component.text("Has a huge cave room underground")
        ),
        Location(Main.WORLD, -210.5, 39.00, -142.5, 135f, 25f),
    ),

    ICY_RAVINE(
        MapType.CLASSIC, setOf(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
        BotBowsTeam.GRAUT, BotBowsTeam.WACKY,
        Menu.makeItem(
            Material.SPRUCE_SAPLING, Component.text("Icy Ravine", NamedTextColor.AQUA),
            Component.text("Graut", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Wacky", NamedTextColor.GREEN)),
            Component.text("A flat arena in a spruce forest with ice spikes and igloos"),
            Component.text("Has a huge ravine in the middle and many caves underground")
        ),
        Location(Main.WORLD, -210.5, 39.00, -253.5, 135f, 25f),
    ),

    ROYAL_CASTLE(
        MapType.CLASSIC, setOf(HazardType.GHOST),
        BotBowsTeam.KJØDD, BotBowsTeam.GOOFY,
        Menu.makeItem(
            Material.STONE_BRICK_STAIRS, Component.text("Royal Castle", NamedTextColor.GREEN),
            Component.text("Kjødd", NamedTextColor.DARK_AQUA)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Goofy", NamedTextColor.DARK_GREEN)),
            Component.text("A castle themed arena")
        ),
        Location(Main.WORLD, -230.5, 32.5, -352.5, 165f, 15f),
    ),

    STEAMPUNK(
        MapType.CLASSIC, setOf(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
        BotBowsTeam.BLOCC, BotBowsTeam.QUICC,
        Menu.makeItem(
            Material.COPPER_BULB, Component.text("Steampunk", NamedTextColor.GOLD),
            Component.text("Blocc", NamedTextColor.GOLD)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Quicc", NamedTextColor.AQUA)),
            Component.text("A steampunk themed arena")
        ),
        Location(Main.WORLD, -356.5, 33.0, -352.50, 180f, 15f),
    ),

    PIGLIN_HIDEOUT(
        MapType.CLASSIC, setOf(HazardType.EARTHQUAKE, HazardType.GHOST),
        BotBowsTeam.PIGLIN, BotBowsTeam.HOGLIN,
        Menu.makeItem(
            Material.MAGMA_BLOCK, Component.text("Piglin Hideout", NamedTextColor.RED),
            Component.text("Piglin", NamedTextColor.GOLD)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Hoglin", NamedTextColor.YELLOW)),
            Component.text("A large volcano arena")
        ),
        Location(Main.WORLD, -342.5, 28.5, -173.5, -300f, 30f),
    ),

    INSIDE_BOTBASE(
        MapType.OLD, setOf(HazardType.STORM, HazardType.GHOST),
        BotBowsTeam.CORNER, BotBowsTeam.CORE_INSIDE,
        Menu.makeItem(
            Material.GREEN_GLAZED_TERRACOTTA, Component.text("Inside the BotBase", NamedTextColor.GREEN),
            Component.text("Corner", NamedTextColor.GRAY)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Core", NamedTextColor.GREEN)),
            Component.text("Inside the BotBase building, with lots of"),
            Component.text("wires, batteries, and electricity")
        ),
        Location(Main.WORLD, 7.5, 22.0, -262.5, 60f, 15f),
    ),

    OUTSIDE_BOTBASE(
        MapType.OLD, setOf(HazardType.STORM, HazardType.GHOST),
        BotBowsTeam.CORE_OUTSIDE, BotBowsTeam.MOUNTAIN,
        Menu.makeItem(
            Material.GRASS_BLOCK, Component.text("Outside the BotBase", NamedTextColor.GREEN),
            Component.text("Core", NamedTextColor.GREEN)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Mountain", NamedTextColor.AQUA)),
            Component.text("A field outside the BotBase"),
            Component.text("next to a mountain")
        ),
        Location(Main.WORLD, -69.5, 23.0, -210.5, 135f, 15f),
    ),

    ROCKET_FOREST(
        MapType.OLD, setOf(HazardType.STORM, HazardType.GHOST),
        BotBowsTeam.DOOR, BotBowsTeam.TUNNEL,
        Menu.makeItem(
            Material.SPRUCE_SAPLING, Component.text("Rocket Forest", NamedTextColor.DARK_GREEN),
            Component.text("Door", NamedTextColor.GRAY)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Tunnel", NamedTextColor.DARK_GREEN)),
            Component.text("In the Rocket Forest next to the mountain"),
            Component.text("with a rocket launcher in the middle")
        ),
        Location(Main.WORLD, -70.71, 26.26, -208.68, -7.5f, 15f),
    ),

    ROCKET(
        MapType.OLD, setOf(HazardType.GHOST),
        BotBowsTeam.DROPPER, BotBowsTeam.ENGINE,
        Menu.makeItem(
            Material.CRAFTER, Component.text("Inside the Rocket", NamedTextColor.RED),
            Component.text("Dropper", NamedTextColor.BLACK)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Engine", NamedTextColor.RED)),
            Component.text("Inside the Rocket, including the engine,"),
            Component.text("control panel, and power supply")
        ),
        Location(Main.WORLD, 16.5, 65.5, 33.0, 800f, 0f),
    ),

    SPACE_STATION(
        MapType.OLD, setOf(HazardType.GHOST),
        BotBowsTeam.WARM, BotBowsTeam.COLD,
        Menu.makeItem(
            Material.GLASS, Component.text("Space Station", NamedTextColor.AQUA),
            Component.text("Warm", NamedTextColor.RED)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("Cold", NamedTextColor.AQUA)),
            Component.text("At the space station where you can"),
            Component.text("traverse space tubes in low gravity")
        ),
        Location(Main.WORLD_END, 157.60, 110.22, 160.62, -342.75f, 24.45f),
    ),

    MARS_BASE(
        MapType.OLD, setOf(HazardType.STORM, HazardType.GHOST),
        BotBowsTeam.BLAUD, BotBowsTeam.SAUCE,
        Menu.makeItem(
            Material.RED_SAND, Component.text("Mars Base", NamedTextColor.GOLD),
            Component.text("???", NamedTextColor.GRAY)
                .append(Component.text(" vs ", NamedTextColor.WHITE))
                .append(Component.text("???", NamedTextColor.GRAY)),
            Component.text("At the mars base. Sadly not finished yet,"),
            Component.text("if it ever will be...")
        ),
        Main.WORLD_SPAWN_LOBBY,
    );

    init {
        team1.oppositeTeam = team2
        team2.oppositeTeam = team1
    }

    fun getMenuItem(): ItemStack {
        val item = item.clone()
        item.editMeta {
            it.persistentDataContainer.set(KEY, PersistentDataType.STRING, name)
            it.lore(it.lore()?.apply { add(Component.text("Right click to view map", NamedTextColor.DARK_AQUA)) })
        }
        return item
    }

    fun prettyName(): String {
        return name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    companion object {
        val KEY: NamespacedKey = NamespacedKey("botbows", "selected_map")
    }
}
