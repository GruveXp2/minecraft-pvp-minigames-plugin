package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        PlayerInventory inv = p.getInventory();
        ItemStack item = inv.getItemInMainHand();
        if (item.isSimilar(BotBows.MENU_ITEM)) {
            BotBows.gameMenu.open(p);
        } else if (item.isSimilar(BotBows.SETTINGS_ITEM)) {
            BotBows.accessSettings(p);
        } else if (item.isSimilar(Lobby.NOT_READY)) {
            BotBowsPlayer bp = BotBows.getLobby(p).getBotBowsPlayer(p);
            bp.setReady(true, inv.getHeldItemSlot());
        } else if (item.isSimilar(Lobby.READY)) {
            BotBowsPlayer bp = BotBows.getLobby(p).getBotBowsPlayer(p);
            bp.setReady(false, inv.getHeldItemSlot());
        } else {
            AbilityListener.onAbilityUse(e);
        }
    }
}
