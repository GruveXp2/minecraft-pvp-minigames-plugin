package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.scheduler.BukkitRunnable;

public class BotBowsGiver  extends BukkitRunnable {

    private final Lobby lobby;

    public BotBowsGiver(Lobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public void run() {
        for (BotBowsPlayer p : lobby.getPlayers()) {
            if (!lobby.isGameActive()) {
                cancel();
                return;
            }
            if (!p.isDamaged()) {
                p.reloadBotBow();
            }
        }
    }
}
