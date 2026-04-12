package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class MapVotingMenu extends SettingsMenu {

    public static final ItemStack MAP_CATEGORY_MODERN = makeItem("gear", Component.text("Map category"),
            Component.text("Modern BotBows", NamedTextColor.GREEN),
            Component.text("2023-", NamedTextColor.DARK_GREEN));

    public static final ItemStack MAP_CATEGORY_OLD = makeItem("gear", Component.text("Map category"),
            Component.text("Old BotBows", NamedTextColor.YELLOW),
            Component.text("2019-2020", NamedTextColor.GOLD));

    private boolean isOldMapCategory = false;

    protected MapVotingMenu(Settings settings) {
        super(settings);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Vote for map");
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

        BotBowsPlayer bp = BotBows.getBotBowsPlayer(clicker);
        if (bp == null) return;

        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null)  return;

        String mapStr = clickedItem.getPersistentDataContainer().get(BotBowsMap.KEY, PersistentDataType.STRING);
        if (mapStr != null) {
            BotBowsMap map = BotBowsMap.valueOf(mapStr);
            if (map == BotBowsMap.MARS_BASE) {
                clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED));
                return;
            }
            //settings.setMap(map);
            settings.getMapVotingSession().vote(bp, map);
            return;
        }

        if (clickedItem.getType() == Material.FIREWORK_STAR) {
            if (e.getSlot() == getSlots() - 5) {
                isOldMapCategory = !isOldMapCategory;
                updateMenu();
            }
        }
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
        updateMenu();
        setFillerVoid();
    }
}
