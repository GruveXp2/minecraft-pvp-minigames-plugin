package gruvexp.bbminigames.twtClassic;

import com.google.common.collect.ImmutableSet;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BotBowsMap {
    CLASSIC_ARENA(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
            Menu.makeItem(Material.SLIME_BALL, Component.text("Classic Arena", NamedTextColor.GRAY),
                    Component.text("Blaud", NamedTextColor.BLUE)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Sauce", NamedTextColor.RED)),
                    Component.text("A flat arena with modern royal style"),
                    Component.text("Has a huge cave room underground"))),

    ICY_RAVINE(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
            Menu.makeItem(Material.SPRUCE_SAPLING, Component.text("Icy Ravine", NamedTextColor.AQUA),
                    Component.text("Graut", NamedTextColor.LIGHT_PURPLE)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Wacky", NamedTextColor.GREEN)),
                    Component.text("A flat arena in a spruce forest with ice spikes and igloos"),
                    Component.text("Has a huge ravine in the middle and many caves underground"))),

    ROYAL_CASTLE(ImmutableSet.of(HazardType.GHOST),
            Menu.makeItem(Material.STONE_BRICK_STAIRS, Component.text("Royal Castle", NamedTextColor.GREEN),
                    Component.text("Kjødd", NamedTextColor.DARK_AQUA)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Goofy", NamedTextColor.DARK_GREEN)),
                    Component.text("A castle themed arena"))),
    STEAMPUNK(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST),
            Menu.makeItem(Material.COPPER_BULB, Component.text("Steampunk", NamedTextColor.GOLD),
                    Component.text("Blocc", NamedTextColor.GOLD)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Quicc", NamedTextColor.AQUA)),
                    Component.text("A steampunk themed arena"))),

    PIGLIN_HIDEOUT(ImmutableSet.of(HazardType.EARTHQUAKE, HazardType.GHOST),
            Menu.makeItem(Material.MAGMA_BLOCK, Component.text("Piglin Hideout", NamedTextColor.RED),
                    Component.text("Piglin", NamedTextColor.GOLD)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Hoglin", NamedTextColor.YELLOW)),
                    Component.text("A large volcano arena"))),

    INSIDE_BOTBASE(ImmutableSet.of(HazardType.STORM, HazardType.GHOST),
            Menu.makeItem(Material.GREEN_GLAZED_TERRACOTTA, Component.text("Inside the BotBase", NamedTextColor.GREEN),
                    Component.text("Corner", NamedTextColor.GRAY)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Core", NamedTextColor.GREEN)),
                    Component.text("Inside the BotBase building, with lots of"),
                    Component.text("wires, batteries, and electricity"))),

    OUTSIDE_BOTBASE(ImmutableSet.of(HazardType.STORM, HazardType.GHOST),
            Menu.makeItem(Material.GRASS_BLOCK, Component.text("Outside the BotBase", NamedTextColor.GREEN),
                    Component.text("Core", NamedTextColor.GREEN)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Mountain", NamedTextColor.AQUA)),
                    Component.text("A field outside the BotBase"),
                    Component.text("next to a mountain"))),

    ROCKET_FOREST(ImmutableSet.of(HazardType.STORM, HazardType.GHOST),
            Menu.makeItem(Material.SPRUCE_SAPLING, Component.text("Rocket Forest", NamedTextColor.DARK_GREEN),
                    Component.text("Door", NamedTextColor.GRAY)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Tunnel", NamedTextColor.DARK_GREEN)),
                    Component.text("In the Rocket Forest next to the mountain"),
                    Component.text("with a rocket launcher in the middle"))),

    ROCKET(ImmutableSet.of(HazardType.GHOST),
            Menu.makeItem(Material.CRAFTER, Component.text("Inside the Rocket", NamedTextColor.RED),
                    Component.text("Dropper", NamedTextColor.BLACK)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Engine", NamedTextColor.RED)),
                    Component.text("Inside the Rocket, including the engine,"),
                    Component.text("control panel, and power supply"))),

    SPACE_STATION(ImmutableSet.of(HazardType.GHOST),
            Menu.makeItem(Material.GLASS, Component.text("Space Station", NamedTextColor.AQUA),
                    Component.text("Warm", NamedTextColor.RED)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("Cold", NamedTextColor.AQUA)),
                    Component.text("At the space station where you can"),
                    Component.text("traverse space tubes in low gravity"))),

    MARS_BASE(ImmutableSet.of(HazardType.STORM, HazardType.GHOST),
            Menu.makeItem(Material.RED_SAND, Component.text("Mars Base", NamedTextColor.GOLD),
                    Component.text("???", NamedTextColor.GRAY)
                            .append(Component.text(" vs ", NamedTextColor.WHITE))
                            .append(Component.text("???", NamedTextColor.GRAY)),
                    Component.text("At the mars base. Sadly not finished yet,"),
                    Component.text("if it ever will be...")));

    public final ImmutableSet<HazardType> allowedHazards;
    private final ItemStack menuItem;

    BotBowsMap(ImmutableSet<HazardType> allowedHazards, ItemStack menuItem) {
        this.allowedHazards = allowedHazards;
        this.menuItem = menuItem;
    }

    public ItemStack getMenuItem() {
        return menuItem.clone();
    }
}
