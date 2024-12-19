package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class BotBowsGiver  extends BukkitRunnable {

    @Override
    public void run() {
        for (BotBowsPlayer p : BotBows.getPlayers()) {
            if (!BotBows.activeGame) {
                cancel();
                return;
            }
            if (!p.isDamaged()) {
                p.player.getInventory().setItem(0, BotBows.BOTBOW);
            }
        }
    }
}
