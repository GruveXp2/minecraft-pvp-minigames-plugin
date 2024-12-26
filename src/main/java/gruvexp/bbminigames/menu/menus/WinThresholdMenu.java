package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WinThresholdMenu extends SettingsMenu {
    @Override
    public Component getMenuName() {
        return Component.text("Win threshold (4/6)");
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (!settings.playerIsMod(BotBows.getBotBowsPlayer(clicker)) && !clickedOnBottomButtons(e)) return;

        switch (e.getCurrentItem().getType()) {
            case RED_STAINED_GLASS_PANE -> settings.changeWinThreshold(-10);
            case PINK_STAINED_GLASS_PANE -> settings.changeWinThreshold(-1);
            case LIME_STAINED_GLASS_PANE -> settings.changeWinThreshold(1);
            case GREEN_STAINED_GLASS_PANE -> settings.changeWinThreshold(10);
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.healthMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.hazardMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems();
        ItemStack sub10 = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("-10"));
        ItemStack sub1= makeItem(Material.PINK_STAINED_GLASS_PANE, Component.text("-1"));
        ItemStack add1 = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("+1"));
        ItemStack add10 = makeItem(Material.GREEN_STAINED_GLASS_PANE, Component.text("+10"));
        ItemStack is = makeItem(Material.BLUE_TERRACOTTA, Component.text("Win score threshold", NamedTextColor.BLUE));
        is.setAmount(settings.getWinThreshold());

        inventory.setItem(2, sub10);
        inventory.setItem(3, sub1);
        inventory.setItem(4, is);
        inventory.setItem(5, add1);
        inventory.setItem(6, add10);

        setPageButtons(1, true, true);
        setFillerVoid();
    }
    public void updateMenu() {
        ItemStack is;
        if (settings.getWinThreshold() > 0) {
            is = makeItem(Material.BLUE_TERRACOTTA, Component.text("Win score threshold", NamedTextColor.BLUE));
            is.setAmount(settings.getWinThreshold());
        } else {
            is = makeItem(Material.YELLOW_TERRACOTTA, Component.text("Infinite rounds", NamedTextColor.YELLOW), "Run /stopgame to stop the game");
        }
        inventory.setItem(4, is);
    }
}
