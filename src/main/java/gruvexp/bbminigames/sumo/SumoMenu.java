package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.menu.Menu;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SumoMenu extends Menu {

    public SumoMenu() {
        inventory.setItem(2, TOURNAMENTS);
        inventory.setItem(6, CROWN);
    }

    ItemStack TOURNAMENTS = Menu.makeItem(Material.TUBE_CORAL_FAN, Component.text("Tournaments"),
            Component.text("Everybody plays against"), Component.text("everyone, and the one"), Component.text("with most points win"));

    //golden helmet
    ItemStack CROWN = Menu.makeItem(Material.GOLDEN_HELMET, Component.text("Crown"),
            Component.text("The one who survives the"), Component.text("longest with the crown, wins"));

    @Override
    public Component getMenuName() {
        return Component.text(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Menu");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem().getType() == Material.TUBE_CORAL_FAN) {
            if (SumoData.playerList.contains(p)) {
                p.sendMessage(ChatColor.RED + "Nothing happened, you already joined. Ther are currently " + SumoData.playerList.size() + " players.");
                e.setCancelled(true);//disable item movements
                return;
            } //cant join the same game many times
            p.sendMessage(ChatColor.GREEN + "Joining SUMO Tournaments" );
            p.teleport(new Location(p.getWorld(),-114.7, 36.0, -126.3));
            p.setGameMode(GameMode.ADVENTURE);
            SumoData.playerList.add(p);

            for (Player q : Bukkit.getOnlinePlayers()) {
                q.sendMessage(ChatColor.YELLOW + p.getPlayerListName() + " joined Sumo! (" + SumoData.playerList.size() + ")");
            }

            for (Player q : SumoData.playerList) {
                p.sendMessage(q.getPlayerListName());
            }
        } else if (e.getCurrentItem().getType() == Material.GOLDEN_HELMET) {
            p.sendMessage(ChatColor.GREEN + "Joining SUMO Crown" );
            p.teleport(new Location(p.getWorld(),-114.7, 36.0, -126.3));
        }
        e.setCancelled(true);//disable item movements
    }
}
