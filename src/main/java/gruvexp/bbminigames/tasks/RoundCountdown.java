package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.Component;
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
                    BotBows.messagePlayers(Component.text("BotBows Classic ", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                            .append(Component.text("is starting in "))
                            .append(Component.text(String.valueOf(5 - time), NamedTextColor.GOLD)));
            case 5 -> {
                BotBows.messagePlayers(Component.text("BotBows Classic ", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text("has started!")));
                botBowsGame.canMove = true;
                botBowsGame.triggerHazards();
                cancel(); // stopper loopen
            }
        }
        time++;
    }
}
