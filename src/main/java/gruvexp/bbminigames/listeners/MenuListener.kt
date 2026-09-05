package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.menu.Menu
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class MenuListener : Listener {
    @EventHandler
    fun onMenuClick(e: InventoryClickEvent) {
        // holder is the entity/block etc who owns the inventory, but since the menus are also holders of their inventory
        // it means if a menu owns it, we can get its reference from the inventory alone
        val holder = e.inventory.holder
        if (holder is Menu) {
            if (e.clickedInventory != e.whoClicked.inventory) { // only cancel clicks in the menu
                e.isCancelled = true
            }
            holder.handleMenu(e) // menu logic
        }
    }
}


