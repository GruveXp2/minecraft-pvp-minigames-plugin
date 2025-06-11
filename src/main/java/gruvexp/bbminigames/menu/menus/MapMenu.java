package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MapMenu extends SettingsMenu {

    public static final ItemStack MAP_CATEGORY_MODERN = makeItem("gear", Component.text("Map category"),
            Component.text("Modern BotBows", NamedTextColor.GREEN),
            Component.text("2023-", NamedTextColor.DARK_GREEN));

    public static final ItemStack MAP_CATEGORY_OLD = makeItem("gear", Component.text("Map category"),
            Component.text("Old BotBows", NamedTextColor.YELLOW),
            Component.text("2019-2020", NamedTextColor.GOLD));


    public static final ItemStack CLASSIC_ARENA = makeItem(Material.SLIME_BALL, Component.text("Classic Arena", NamedTextColor.GRAY),
            Component.text("Blaud", NamedTextColor.BLUE)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Sauce", NamedTextColor.RED)),
            Component.text("A flat arena with modern royal style"),
            Component.text("Has a huge cave room underground"));

    public static final ItemStack ICY_RAVINE = makeItem(Material.SPRUCE_SAPLING, Component.text("Icy Ravine", NamedTextColor.AQUA),
            Component.text("Graut", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Wacky", NamedTextColor.GREEN)),
            Component.text("A flat arena in a spruce forest with ice spikes and igloos"),
            Component.text("Has a huge ravine in the middle and many caves underground"));

    public static final ItemStack ROYAL_CASTLE = makeItem(Material.STONE_BRICK_STAIRS, Component.text("Royal Castle", NamedTextColor.GREEN),
            Component.text("Kjødd", NamedTextColor.DARK_AQUA)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Goofy", NamedTextColor.DARK_GREEN)),
            Component.text("A castle themed arena"),
            Component.text("Work in progress", NamedTextColor.YELLOW));

    public static final ItemStack STEAMPUNK = makeItem(Material.COPPER_BULB, Component.text("Steampunk", NamedTextColor.GOLD),
            Component.text("Blocc", NamedTextColor.GOLD)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Quicc", NamedTextColor.AQUA)),
            Component.text("A steampunk themed arena"));

    public static final ItemStack PIGLIN_HIDEOUT = makeItem(Material.MAGMA_BLOCK, Component.text("Piglin Hideout", NamedTextColor.RED),
            Component.text("Piglin", NamedTextColor.GOLD)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Hoglin", NamedTextColor.YELLOW)),
            Component.text("A large volcano arena"));

    public static final ItemStack INSIDE_BOTBASE = makeItem(Material.GREEN_GLAZED_TERRACOTTA, Component.text("Inside the BotBase", NamedTextColor.GREEN),
            Component.text("Corner", NamedTextColor.GRAY)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Core", NamedTextColor.GREEN)),
            Component.text("Inside the BotBase building, with lots of"),
            Component.text("wires, batteries, and electricity"));

    public static final ItemStack OUTSIDE_BOTBASE = makeItem(Material.GRASS_BLOCK, Component.text("Outside the BotBase", NamedTextColor.GREEN),
            Component.text("Core", NamedTextColor.GREEN)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Mountain", NamedTextColor.AQUA)),
            Component.text("A field outside the BotBase"),
            Component.text("next to a mountain"));

    public static final ItemStack ROCKET_FOREST = makeItem(Material.SPRUCE_SAPLING, Component.text("Rocket Forest", NamedTextColor.DARK_GREEN),
            Component.text("Door", NamedTextColor.GRAY)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Tunnel", NamedTextColor.DARK_GREEN)),
            Component.text("In the Rocket Forest next to the mountain"),
            Component.text("with a rocket launcher in the middle"));

    public static final ItemStack ROCKET = makeItem(Material.CRAFTER, Component.text("Inside the Rocket", NamedTextColor.RED),
            Component.text("Dropper", NamedTextColor.BLACK)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Engine", NamedTextColor.RED)),
            Component.text("Inside the Rocket, including the engine,"),
            Component.text("control panel, and power supply"));

    public static final ItemStack SPACE_STATION = makeItem(Material.GLASS, Component.text("Space Station", NamedTextColor.AQUA),
            Component.text("Warm", NamedTextColor.RED)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("COLD", NamedTextColor.AQUA)),
            Component.text("At the space station where you can"),
            Component.text("traverse space tubes in low gravity"));

    public static final ItemStack MARS = makeItem(Material.RED_SAND, Component.text("Mars Base", NamedTextColor.GOLD),
            Component.text("???", NamedTextColor.GRAY)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("???", NamedTextColor.GRAY)),
            Component.text("At the mars base. Sadly not finished yet,"),
            Component.text("if it ever will be..."));

    private boolean isOldMapCategory = false;

    public MapMenu(Settings settings) {
        super(settings);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Arena map (1/6)");
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (e.getClickedInventory() != inventory) return;
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

        switch (e.getCurrentItem().getType()) {
            case SLIME_BALL -> settings.setMap(BotBowsMap.CLASSIC_ARENA);
            case SPRUCE_SAPLING -> {
                if (isOldMapCategory) settings.setMap(BotBowsMap.ROCKET_FOREST);
                else                  settings.setMap(BotBowsMap.ICY_RAVINE);
            }
            case MAGMA_BLOCK -> settings.setMap(BotBowsMap.PIGLIN_HIDEOUT);
            case COPPER_BULB -> settings.setMap(BotBowsMap.STEAMPUNK);
            case STONE_BRICK_STAIRS, RED_SAND -> clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED));

            case GREEN_GLAZED_TERRACOTTA -> settings.setMap(BotBowsMap.INSIDE_BOTBASE);
            case GRASS_BLOCK -> settings.setMap(BotBowsMap.OUTSIDE_BOTBASE);
            case CRAFTER -> settings.setMap(BotBowsMap.ROCKET);
            case GLASS -> settings.setMap(BotBowsMap.SPACE_STATION);

            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 4) {
                    settings.teamsMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 5) {
                    isOldMapCategory = !isOldMapCategory;
                    updateMenu();
                }
            }
        }
    }

    public void updateMenu() {
        if (isOldMapCategory) {
            inventory.setItem(1, INSIDE_BOTBASE);
            inventory.setItem(2, OUTSIDE_BOTBASE);
            inventory.setItem(3, ROCKET_FOREST);
            inventory.setItem(4, VOID);
            inventory.setItem(5, ROCKET);
            inventory.setItem(6, SPACE_STATION);
            inventory.setItem(7, MARS);
            inventory.setItem(13, MAP_CATEGORY_OLD);
        } else {
            inventory.setItem(1, VOID);
            inventory.setItem(2, CLASSIC_ARENA);
            inventory.setItem(3, ICY_RAVINE);
            inventory.setItem(4, ROYAL_CASTLE);
            inventory.setItem(5, STEAMPUNK);
            inventory.setItem(6, PIGLIN_HIDEOUT);
            inventory.setItem(7, VOID);
            inventory.setItem(13, MAP_CATEGORY_MODERN);
        }
    }

    @Override
    public void setMenuItems() {
        setPageButtons(1, false, true);
        updateMenu();
        setFillerVoid();
    }
}
