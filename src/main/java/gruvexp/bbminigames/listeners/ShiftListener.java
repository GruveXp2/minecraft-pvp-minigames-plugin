package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.SneakCoolDown;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Cooldowns;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
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
        if (p.isSneaking()) {
            lobby.botBowsGame.barManager.setSneakBarColor(p, ChatColor.RED, BarColor.RED);
            return;
        }

        if (Cooldowns.sneakCooldowns.get(p) <= 0) {
            Cooldowns.sneakRunnables.put(p, new SneakCoolDown(p).runTaskTimer(Main.getPlugin(), 0L, 1L));
            lobby.botBowsGame.barManager.setSneakBarVisibility(p, true);
        } else {
            e.setCancelled(true);
            p.setSneaking(false);
        }
    }

}
