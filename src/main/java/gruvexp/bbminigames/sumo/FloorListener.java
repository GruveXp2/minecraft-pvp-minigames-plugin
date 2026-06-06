package gruvexp.bbminigames.sumo;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;

public class FloorListener implements Listener {

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!Arrays.asList(SumoData.getBattle()).contains(p)) return;
        if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.RED_STAINED_GLASS) return;
        if (p.getGameMode() != GameMode.ADVENTURE) return;

        Player q = SumoData.getBattle()[Math.abs(Arrays.asList(SumoData.getBattle()).indexOf(p) - 1)];
        p.sendMessage(q.getPlayerListName()+" Won the match!");
        q.sendMessage("You won the match!");
        Board.saveResult(q, true);
        Board.saveResult(p, false); //p er den som hitta golvet

        p.teleport(new Location(Bukkit.getWorld("Sumo"), 33.5, 31.0, -200.5));
        q.teleport(new Location(Bukkit.getWorld("Sumo"), 33.5, 31.0, -200.5));

        SumoData.postBattle(); // kalkulerer score
        SumoData.startNextBattle(); //går videre til neste

    }

}
