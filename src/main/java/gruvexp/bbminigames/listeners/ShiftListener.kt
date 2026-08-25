package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent

class ShiftListener : Listener {
    @EventHandler
    fun onShiftToggle(e: PlayerToggleSneakEvent) {
        val p = e.getPlayer()
        val bp = BotBows.getBotBowsPlayer(p) ?: return
        if (!bp.lobby.isGameActive) return
        if (p.gameMode != GameMode.ADVENTURE) return


        if (bp.isSneakingExhausted && e.isSneaking) {
            p.sendActionBar(Component.text("You are exhausted and cant sneak", NamedTextColor.RED))
            e.isCancelled = true
        }
    }
}
