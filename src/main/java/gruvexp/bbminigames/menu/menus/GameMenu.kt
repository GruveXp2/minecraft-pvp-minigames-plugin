package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.sumo.SumoManager;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GameMenu extends Menu {

    public GameMenu() {
        ItemStack botbows = makeItem(Material.BOW, Component.text("BotBows Classic"), Component.text("The classic game of BotBows"));
        ItemStack sumo = makeItem(Material.STICK, Component.text("Sumo"), Component.text("The Sumo minigame"));
        inventory.setItem(3, botbows);
        inventory.setItem(5, sumo);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Game Menu");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        if (item.getType() == Material.BOW) {
            BotBows.lobbyMenu.open(p);
        } else if (item.getType() == Material.STICK) {
            SumoManager.sumoMenu.open(p);
        }
    }
}
