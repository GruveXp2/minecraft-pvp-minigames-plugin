package dev.gruve.minigames.listeners

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import dev.gruve.minigames.botbows.BotBows
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val p = e.player
        BotBows.getBotBowsPlayer(p)?.let { bp ->
            bp.lobby.botBowsGame?.handleMovement(e, bp)
        } ?: run {
            BotBows.handleMovement(e)
        }
    }

    @EventHandler
    fun onJump(e: PlayerJumpEvent) {
        val p = e.player
        if (!BotBows.isPlayerJoined(p)) {
            BotBows.handleJump(e)
            return
        }
        BotBows.getLobby(p)?.botBowsGame?.handleJump(e)
    }
}
