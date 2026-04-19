package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;

        switch (clickedItem.getType()) {
            case SLIME_BALL -> settings.setMap(BotBowsMap.CLASSIC_ARENA);
            case SPRUCE_SAPLING -> {
                if (isOldMapCategory) settings.setMap(BotBowsMap.ROCKET_FOREST);
                else                  settings.setMap(BotBowsMap.ICY_RAVINE);
            }
            case MAGMA_BLOCK -> settings.setMap(BotBowsMap.PIGLIN_HIDEOUT);
            case COPPER_BULB -> settings.setMap(BotBowsMap.STEAMPUNK);
            case STONE_BRICK_STAIRS -> settings.setMap(BotBowsMap.ROYAL_CASTLE);
            case RED_SAND -> clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED));

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

    private void sendExperimentalLockedMessage(Player player) {
        player.sendMessage(Component.text("This map is not fully added yet. To play on it, run ", NamedTextColor.YELLOW).append(Component.text("/test toggle_experimental", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/test toggle_experimental")));
    }

    public void updateMenu() {
        if (isOldMapCategory) {
            inventory.setItem(1, BotBowsMap.INSIDE_BOTBASE.getMenuItem());
            inventory.setItem(2, BotBowsMap.OUTSIDE_BOTBASE.getMenuItem());
            inventory.setItem(3, BotBowsMap.ROCKET_FOREST.getMenuItem());
            inventory.setItem(4, VOID);
            inventory.setItem(5, BotBowsMap.ROCKET.getMenuItem());
            inventory.setItem(6, BotBowsMap.SPACE_STATION.getMenuItem());
            inventory.setItem(7, BotBowsMap.MARS_BASE.getMenuItem());
            inventory.setItem(13, MAP_CATEGORY_OLD);
        } else {
            inventory.setItem(1, VOID);
            inventory.setItem(2, BotBowsMap.CLASSIC_ARENA.getMenuItem());
            inventory.setItem(3, BotBowsMap.ICY_RAVINE.getMenuItem());
            inventory.setItem(4, BotBowsMap.ROYAL_CASTLE.getMenuItem());
            inventory.setItem(5, BotBowsMap.STEAMPUNK.getMenuItem());
            inventory.setItem(6, BotBowsMap.PIGLIN_HIDEOUT.getMenuItem());
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
