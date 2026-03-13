package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.Material;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class RightClickListener implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e) {
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
                break;
            case RIGHT_CLICK_BLOCK:
                Player p = e.getPlayer();
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
                if (bp == null) break;
                Material type = e.getClickedBlock().getType();
                if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL) { // stop accidentally opening containers mid-game
                    e.setCancelled(true);
                    return;
                }
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
            BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
            bp.setReady(true, inv.getHeldItemSlot());
        } else if (item.isSimilar(Lobby.READY)) {
            BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
            bp.setReady(false, inv.getHeldItemSlot());
        } else if (e.getClickedBlock() != null && e.getClickedBlock().getType().data == TrapDoor.class) {
            // toggling copper trapdoors is not allowed ingame, they should behave like other metal trapdoors like iron
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            if (lobby.isGameActive()) e.setCancelled(true);
        } else {
            AbilityListener.onAbilityUse(e);
        }
    }
}
