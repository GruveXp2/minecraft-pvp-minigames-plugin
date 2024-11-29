package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundCountdown extends BukkitRunnable { // LANGUAGE LEVEL = 14

    final BotBowsGame botBowsGame;
    int time = 0;

    public RoundCountdown(BotBowsGame botBowsGame) {
        this.botBowsGame = botBowsGame;
    }

    @Override
    public void run() {
        switch (time) {
            case 0, 1, 2, 3, 4 ->
                    BotBows.messagePlayers(ChatColor.BOLD + "" + ChatColor.GREEN + "BotBows Classic " + ChatColor.RESET + "is starting in " + ChatColor.GOLD + (5 - time));
            case 5 -> {
                BotBows.messagePlayers(ChatColor.BOLD + "" + ChatColor.GREEN + "BotBows Classic " + ChatColor.RESET + "has started!");
                botBowsGame.canMove = true;
                botBowsGame.triggerHazards();
                cancel(); // stopper loopen
            }
        }
        time++;
    }
}
