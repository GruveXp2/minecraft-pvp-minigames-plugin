package gruvexp.bbminigames.menu

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.persistence.PersistentDataType

abstract class PaginatedMenu : Menu() {
    enum class PageAction {
        PREV,
        NEXT
    }

    protected fun setPageButtons(rowIndex: Int, prevMenuButton: Boolean, nextMenuButton: Boolean) {
        inventory.setItem(rowIndex * 9    , VOID)
        inventory.setItem(rowIndex * 9 + 1, VOID)
        inventory.setItem(rowIndex * 9 + 2, VOID)
        inventory.setItem(rowIndex * 9 + 3, if (prevMenuButton) PAGE_PREV else VOID)
        inventory.setItem(rowIndex * 9 + 4, VOID)
        inventory.setItem(rowIndex * 9 + 5, if (nextMenuButton) PAGE_NEXT else VOID)
        inventory.setItem(rowIndex * 9 + 6, VOID)
        inventory.setItem(rowIndex * 9 + 7, VOID)
        inventory.setItem(rowIndex * 9 + 8, VOID)
    }

    protected open fun prevPage(p: Player) {}
    protected open fun nextPage(p: Player) {}

    protected fun handlePageClick(e: InventoryClickEvent): Boolean {
        val clickedItem = e.getCurrentItem() ?: return false
        if (!clickedItem.hasItemMeta()) return false

        val actionStr = clickedItem.itemMeta.persistentDataContainer
            .get(KEY_PAGE_ACTION, PersistentDataType.STRING) ?: return false

        val p = e.whoClicked as Player
        val action = PageAction.valueOf(actionStr)

        if (action == PageAction.PREV) {
            prevPage(p)
        } else if (action == PageAction.NEXT) {
            nextPage(p)
        }
        return true
    }

    companion object {
        val KEY_PAGE_ACTION: NamespacedKey = NamespacedKey("botbows", "page_action")

        val PAGE_PREV = makeItem("prev", Component.text("Prev"), KEY_PAGE_ACTION, PageAction.PREV.name)
        val PAGE_NEXT = makeItem("next", Component.text("Next"), KEY_PAGE_ACTION, PageAction.NEXT.name)
    }
}
