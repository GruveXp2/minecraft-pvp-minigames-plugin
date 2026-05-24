package gruvexp.bbminigames.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public abstract class PaginatedMenu extends Menu {

    public enum PageAction {
        PREV,
        NEXT
    }
    public static final NamespacedKey KEY_PAGE_ACTION = new NamespacedKey("botbows", "page_action");

    public static final ItemStack PAGE_PREV = makeItem("prev", Component.text("Prev"), KEY_PAGE_ACTION, PageAction.PREV.name());
    public static final ItemStack PAGE_NEXT = makeItem("next", Component.text("Next"), KEY_PAGE_ACTION, PageAction.NEXT.name());

    protected void setPageButtons(int rowIndex, boolean prevMenuButton, boolean nextMenuButton) {
        inventory.setItem(rowIndex*9    , VOID);
        inventory.setItem(rowIndex*9 + 1, VOID);
        inventory.setItem(rowIndex*9 + 2, VOID);
        inventory.setItem(rowIndex*9 + 3, prevMenuButton ? PAGE_PREV : VOID);
        inventory.setItem(rowIndex*9 + 4, VOID);
        inventory.setItem(rowIndex*9 + 5, nextMenuButton ? PAGE_NEXT : VOID);
        inventory.setItem(rowIndex*9 + 6, VOID);
        inventory.setItem(rowIndex*9 + 7, VOID);
        inventory.setItem(rowIndex*9 + 8, VOID);
    }

    protected void prevPage(Player p) {}
    protected void nextPage(Player p) {}

    protected boolean handlePageClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return false;

        String actionStr = clickedItem.getItemMeta().getPersistentDataContainer()
                .get(KEY_PAGE_ACTION, PersistentDataType.STRING);

        if (actionStr == null) return false;

        Player p = (Player) e.getWhoClicked();
        PageAction action = PageAction.valueOf(actionStr);

        if (action == PageAction.PREV) {
            prevPage(p);
        } else if (action == PageAction.NEXT) {
            nextPage(p);
        }
        return true;
    }
}
