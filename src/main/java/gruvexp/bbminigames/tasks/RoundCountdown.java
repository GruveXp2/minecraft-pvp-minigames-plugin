package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundCountdown extends BukkitRunnable {

    final BotBowsGame botBowsGame;
    final int round;
    int time = 0;

    public RoundCountdown(BotBowsGame botBowsGame, int round) {
        this.botBowsGame = botBowsGame;
        this.round = round;
    }

    @Override
    public void run() {
        switch (time) {
            case 0, 1, 2, 3, 4 ->
                    botBowsGame.lobby.messagePlayers(Component.text("Round " + round, Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                            .append(Component.text(" is starting in "))
                            .append(Component.text(String.valueOf(5 - time), NamedTextColor.GOLD)));
            case 5 -> {
                botBowsGame.lobby.messagePlayers(Component.text("Round " + round, Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text(" has started!")));
                botBowsGame.canMove = true;
                botBowsGame.canShoot = true;

                botBowsGame.triggerHazards();

                if (botBowsGame.settings.rain > 0 && !botBowsGame.settings.getHazard(HazardType.STORM).isActive()) {
                    Main.WORLD.setStorm(true);
                    Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerWeather(WeatherType.CLEAR));
                }
                cancel();
            }
        }
        time++;
    }
}
