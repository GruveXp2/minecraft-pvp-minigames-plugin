package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickListener implements Listener {

    @EventHandler
    public void onPlayerRightClickCompass(PlayerInteractEvent event) {
        // Check if the action is a right-click (block or air)
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        // hvis playeren holder kompass, så åpnes game menuet
        Player p = event.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
            BotBows.gameMenu.open(p);
        }
    }
}
