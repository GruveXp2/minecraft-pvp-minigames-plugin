package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickListener implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e) {
        // Check if the action is a right-click (block or air)
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Player p = e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
            BotBows.gameMenu.open(p);
        } else {
            AbilityListener.onAbilityUse(e);
        }
    }
}
