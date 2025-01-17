package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundTimer extends BukkitRunnable {

    final BotBowsGame botBowsGame;
    int time;

    public RoundTimer(BotBowsGame botBowsGame, int minutes) {
        this.botBowsGame = botBowsGame;
        this.time = minutes * 60;
    }

    @Override
    public void run() {
        switch (time) {
            case 300, 180, 120, 60 -> botBowsGame.lobby.messagePlayers(Component.text("Round ends in ")
                    .append(Component.text(time/60, NamedTextColor.YELLOW))
                    .append(Component.text(" minute" + (time == 60 ? "" : "s"))));
            case 30, 10 -> botBowsGame.lobby.messagePlayers(Component.text("Round ends in ", NamedTextColor.YELLOW)
                    .append(Component.text(time, NamedTextColor.GOLD))
                    .append(Component.text(" seconds", NamedTextColor.YELLOW)));
            case 0 -> {
                botBowsGame.endGameTimeout();
                cancel(); // stopper loopen
            }
            default -> {
                if (time % 300 == 0) {
                    botBowsGame.lobby.messagePlayers(Component.text("Round ends in ")
                        .append(Component.text(time/60))
                        .append(Component.text(" minutes")));
                }
            }
        }
        time--;
    }
}
