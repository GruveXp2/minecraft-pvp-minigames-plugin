package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!BotBows.isPlayerJoined(p)) {
            BotBows.handleMovement(e);
            return;
        }
        Lobby lobby = BotBows.getLobby(p);
        if (lobby.isGameActive() && lobby.botBowsGame.canMove) {
            lobby.botBowsGame.handleMovement(e);
        } else {
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            Location spawnPos = bp.getTeam().getSpawnPos(bp);
            if (p.getLocation().getX() == spawnPos.getX() && p.getLocation().getZ() == spawnPos.getZ()) {return;}
            // hvis det er countdown (!canMove), playeren er joina og playeren har gått vekk fra spawn blir han telportert tebake
            p.teleport(spawnPos);
        }
    }
}
