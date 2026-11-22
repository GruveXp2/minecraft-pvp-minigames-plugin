package dev.gruve.minigames.listeners

import dev.gruve.minigames.Main
import dev.gruve.minigames.botbows.BotBows
import dev.gruve.minigames.botbows.ability.AbilityType
import dev.gruve.minigames.menu.Menu
import dev.gruve.minigames.menu.menus.AbilityMenu
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class MenuListener : Listener {
    @EventHandler
    fun onMenuClick(e: InventoryClickEvent) {
        // holder is the entity/block etc who owns the inventory, but since the menus are also holders of their inventory
        // it means if a menu owns it, we can get its reference from the inventory alone
        val isCorrectMenu = checkForCorrectMenu(e)
        if (!isCorrectMenu) return

        val holder = e.inventory.holder
        if (holder is Menu) {
            if (e.clickedInventory != e.whoClicked.inventory) { // only cancel clicks in the menu
                e.isCancelled = true
            }
            holder.handleMenu(e) // menu logic
        }
    }

    fun checkForCorrectMenu(e: InventoryClickEvent): Boolean {

        val p = e.whoClicked as Player
        val bp = BotBows.getBotBowsPlayer(p) ?: return true

        val item = e.currentItem ?: return true

        if (AbilityType.fromItem(item) == null && item.type != Material.ARROW) return true

        val isCorrectMenu = e.inventory.holder is AbilityMenu
        if (isCorrectMenu) return true

        Bukkit.getScheduler().runTaskLater(Main.plugin,
            Runnable { bp.lobby.settings.abilityMenus.getValue(bp).open(p) }, 1)

        e.isCancelled = true
        return false
    }
}


