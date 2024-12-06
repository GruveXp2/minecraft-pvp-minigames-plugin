package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityMenu extends SettingsMenu {
    @Override
    public String getMenuName() {
        return "Abilities";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {

            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.winThresholdMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {

    }

    public void updateMaxAbilities() {
        int maxAbilities = settings.getMaxAbilities();
        for (int i = 0; i < 3; i++) {
            ItemStack is = makeItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + String.valueOf(i + 1));
            if (i < maxAbilities) {
                is = makeItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.RED + String.valueOf(i + 1));
            }
            inventory.setItem(i + 11, is);
        }
    }
}
