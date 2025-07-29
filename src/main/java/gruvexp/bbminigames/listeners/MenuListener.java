package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.menu.menus.AbilityMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){

        InventoryHolder holder = e.getInventory().getHolder();
        // If the inventoryholder of the inventory clicked on is an instance of Menu, then gg. The reason that
        // an InventoryHolder can be a Menu is because our Menu class implements InventoryHolder
        if (holder instanceof Menu menu) {
            if (e.getClickedInventory() != e.getWhoClicked().getInventory()) { // only cancel clicks in the menu
                e.setCancelled(true);
            }
            // Since we know our inventoryholder is a menu, get the Menu Object representing the menu we clicked on
            // Call the handleMenu object which takes the event and processes it
            menu.handleMenu(e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AbilityMenu menu) {
            menu.handleMenuClose(e);
        }
    }
}


