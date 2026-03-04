package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ShiftListener implements Listener {

    @EventHandler
    public void onShiftToggle(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        if (!lobby.isGameActive()) {return;}
        if (p.getGameMode() != GameMode.ADVENTURE) {return;}
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);

        if (bp.isSneakingExhausted() && e.isSneaking()) {
            BotBows.debugMessage("its not allowed to sneak");
            e.setCancelled(true);
        }
    }
}
