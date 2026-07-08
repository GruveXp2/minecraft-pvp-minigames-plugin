package gruvexp.bbminigames.tasks

import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scheduler.BukkitRunnable

class RoundTimer(val botBowsGame: BotBowsGame, minutes: Int) : BukkitRunnable() {
    var time: Int

    init {
        this.time = minutes * 60
    }

    override fun run() {
        when (time) {
            300, 180, 120, 60 -> botBowsGame.lobby.messagePlayers(
                Component.text("Round ends in ")
                    .append(Component.text(time / 60, NamedTextColor.YELLOW))
                    .append(Component.text(" minute" + (if (time == 60) "" else "s")))
            )

            30, 10 -> botBowsGame.lobby.messagePlayers(
                Component.text("Round ends in ", NamedTextColor.YELLOW)
                    .append(Component.text(time, NamedTextColor.GOLD))
                    .append(Component.text(" seconds", NamedTextColor.YELLOW))
            )

            0 -> {
                botBowsGame.endGameTimeout()
                cancel() // stopper loopen
            }

            else -> {
                if (time % 300 == 0) {
                    botBowsGame.lobby.messagePlayers(
                        Component.text("Round ends in ")
                            .append(Component.text(time / 60))
                            .append(Component.text(" minutes"))
                    )
                }
            }
        }
        time--
    }
}
