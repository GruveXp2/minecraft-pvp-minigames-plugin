package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.ShutdownManager
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinLeaveListener : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        ShutdownManager.cancelShutdown()
        val p = e.player
        if (p.name !in listOf("ColinStorm", "GruveXp")) { // only admins stay op
            p.isOp = false
        }
        BotBows.getLobby(p)?.reconnect(p)
        if (p.inventory.itemInMainHand.type != Material.AIR) { // dropper itemet de hadde fra før av så det ikke blir sletta
            Main.WORLD.dropItem(p.location, p.inventory.itemInMainHand)
        }
        p.inventory.setItem(0, BotBows.MENU_ITEM)
        p.sendMessage(Component.text("Welcome to BotBows!", NamedTextColor.GREEN, TextDecoration.BOLD))
        p.sendMessage(
            Component.text("To join a game, run ")
                .append(Component.text("/menu ", NamedTextColor.AQUA).clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    ClickEvent.Payload.string("/menu")
                )))
                .append(Component.text("or right click the compass\n"))
                .append(Component.text("To leave a game, run "))
                .append(Component.text("/botbows leave\n", NamedTextColor.AQUA).clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    ClickEvent.Payload.string("/botbows leave")
                )))
                .append(Component.text("To access settings for a game, run "))
                .append(Component.text("/settings\n", NamedTextColor.AQUA).clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    ClickEvent.Payload.string("/settings")
                )))
                .append(Component.text("To start/stop a game, run "))
                .append(Component.text("/botbows start ", NamedTextColor.AQUA).clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    ClickEvent.Payload.string("/botbows start")
                )))
                .append(Component.text("or "))
                .append(Component.text("/botbows stop", NamedTextColor.AQUA).clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    ClickEvent.Payload.string("/botbows stop")
                )))
        )
    }

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        if (Bukkit.getOnlinePlayers().size == 1) ShutdownManager.scheduleShutdown()
        val p = e.player
        val bp = BotBows.getBotBowsPlayer(p) ?: return
        bp.lobby.disconnect(bp)
    }
}
