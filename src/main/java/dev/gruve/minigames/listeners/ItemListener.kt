package dev.gruve.minigames.listeners

import dev.gruve.minigames.botbows.BotBows
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class ItemListener : Listener {
    @EventHandler
    fun onItemDrop(e: PlayerDropItemEvent) {
        if (BotBows.getBotBowsPlayer(e.player) != null) e.isCancelled = true
    }
}
