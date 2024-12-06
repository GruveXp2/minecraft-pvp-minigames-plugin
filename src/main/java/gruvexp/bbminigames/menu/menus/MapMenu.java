package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class  MapMenu extends SettingsMenu {
    public static final ItemStack ROYAL_ARENA = makeItem(Material.SLIME_BALL, ChatColor.GRAY + "Royal Arena",
            ChatColor.BLUE + "Blaud" + ChatColor.WHITE + " vs " + ChatColor.RED + "Sauce",
            "A flat arena with modern royal style",
            "Has a huge cave room underground");
    public static final ItemStack ICY_RAVINE = makeItem(Material.SPRUCE_SAPLING, ChatColor.AQUA + "Icy Ravine",
            ChatColor.LIGHT_PURPLE + "Graut" + ChatColor.WHITE + " vs " + ChatColor.GREEN + "Wacky",
            "A flat arena in a spruce forest with ice spikes and igloos",
            "Has a huge ravine in the middle and many caves underground");

    @Override
    public String getMenuName() {
        return "Select map";
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case SLIME_BALL -> settings.setMap(BotBowsMap.BLAUD_VS_SAUCE);
            case SPRUCE_SAPLING -> settings.setMap(BotBowsMap.GRAUT_VS_WACKY);
            case FIREWORK_STAR -> settings.teamsMenu.open(clicker);
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems();
        inventory.setItem(3, ROYAL_ARENA);
        inventory.setItem(5, ICY_RAVINE);
        setPageButtons(1, false, true, null);
        setFillerVoid();
    }
}
