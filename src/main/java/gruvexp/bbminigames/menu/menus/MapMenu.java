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
            Component.text("Unnamed 0", NamedTextColor.GRAY)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Unnamed 1", NamedTextColor.GRAY)),
            Component.text("A steampunk themed arena"),
            Component.text("Work in progress", NamedTextColor.YELLOW));

    public static final ItemStack PIGLIN_HIDEOUT = makeItem(Material.MAGMA_BLOCK, Component.text("Piglin Hideout", NamedTextColor.RED),
            Component.text("Unnamed 2", NamedTextColor.GRAY)
                    .append(Component.text(" vs ", NamedTextColor.WHITE))
                    .append(Component.text("Unnamed 3", NamedTextColor.GRAY)),
            Component.text("A large volcano arena"),
            Component.text("Work in progress", NamedTextColor.YELLOW));

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
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

        switch (e.getCurrentItem().getType()) {
            case SLIME_BALL -> settings.setMap(BotBowsMap.CLASSIC_ARENA);
            case SPRUCE_SAPLING -> settings.setMap(BotBowsMap.ICY_RAVINE);
            case STONE_BRICK_STAIRS, COPPER_BULB, MAGMA_BLOCK -> clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED));
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 4) {
                    settings.teamsMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(2, CLASSIC_ARENA);
        inventory.setItem(3, ICY_RAVINE);
        inventory.setItem(4, ROYAL_CASTLE);
        inventory.setItem(5, STEAMPUNK);
        inventory.setItem(6, PIGLIN_HIDEOUT);
        setPageButtons(1, false, true);
        setFillerVoid();
    }
}
