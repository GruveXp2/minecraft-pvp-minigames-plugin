package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.listeners.AbilityListener.Companion.onAbilityUse
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Lobby
import org.bukkit.Material
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class RightClickListener : Listener {
    @EventHandler
    fun onPlayerRightClick(e: PlayerInteractEvent) {
        when (e.action) {
            Action.RIGHT_CLICK_AIR -> {}
            Action.RIGHT_CLICK_BLOCK -> {
                val p = e.getPlayer()
                BotBows.getBotBowsPlayer(p)?.let {
                    val type = e.clickedBlock!!.type
                    if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.BARREL) { // stop accidentally opening containers mid-game
                        e.setCancelled(true)
                        return@onPlayerRightClick
                    }
                }
            }
            else -> return
        }

        val p = e.getPlayer()
        val inv = p.inventory
        val item = inv.itemInMainHand
        when {
            item.isSimilar(BotBows.MENU_ITEM) -> BotBows.gameMenu.open(p)
            item.isSimilar(BotBows.SETTINGS_ITEM) -> BotBows.accessSettings(p)
            item.isSimilar(Lobby.NOT_READY) -> BotBows.getBotBowsPlayer(p)?.setReady(true, inv.heldItemSlot)
            item.isSimilar(Lobby.READY) -> BotBows.getBotBowsPlayer(p)?.setReady(false, inv.heldItemSlot)
            e.clickedBlock?.type?.data == TrapDoor::class.java -> {
                val lobby = BotBows.getLobby(p) ?: return
                if (lobby.isGameActive) e.isCancelled = true
            }
            else -> onAbilityUse(e)
        }
    }
}
