package gruvexp.bbminigames.sumo;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Menu")) return;
        if (event.getCurrentItem() == null) return;

        Player p = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType() == Material.TUBE_CORAL_FAN) {
            if (SumoData.playerList.contains(p)) {
                p.sendMessage(ChatColor.RED + "Nothing happened, you already joined. Ther are currently " + SumoData.playerList.size() + " players.");
                event.setCancelled(true);//disable item movements
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
        } else if (event.getCurrentItem().getType() == Material.GOLDEN_HELMET) {
            p.sendMessage(ChatColor.GREEN + "Joining SUMO Crown" );
            p.teleport(new Location(p.getWorld(),-114.7, 36.0, -126.3));
        }
        event.setCancelled(true);//disable item movements
    }
}
