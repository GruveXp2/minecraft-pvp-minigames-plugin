package gruvexp.bbminigames.tasks

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.WeatherType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class RoundCountdown(val botBowsGame: BotBowsGame, val round: Int) : BukkitRunnable() {
    var time: Int = 0

    override fun run() {
        when (time) {
            0, 1, 2, 3, 4 -> botBowsGame.lobby.messagePlayers(
                Component.text("Round $round", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.text(" is starting in "))
                    .append(Component.text((5 - time).toString(), NamedTextColor.GOLD))
            )

            5 -> {
                botBowsGame.lobby.messagePlayers(
                    Component.text("Round $round", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text(" has started!"))
                )
                botBowsGame.canMove = true
                botBowsGame.canInteract = true

                botBowsGame.triggerHazards()

                if (botBowsGame.settings.rain > 0 && botBowsGame.stormHazard != null && !botBowsGame.stormHazard.isActive) {
                    Main.WORLD.setStorm(true)
                    Bukkit.getOnlinePlayers().forEach { p: Player? -> p!!.setPlayerWeather(WeatherType.CLEAR) }
                }
                cancel()
            }
        }
        time++
    }
}
