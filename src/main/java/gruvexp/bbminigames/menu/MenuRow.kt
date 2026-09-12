package gruvexp.bbminigames.menu

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.min

open class MenuRow(
    protected val inventory: Inventory,
    protected val menuActionId: String?,
    val startSlot: Int, // slotten i inventoriet som man begynner på
    val size: Int // hvor mange slots som blir tatt opp, inkluderer knapper hvis det er det
) {
    enum class RowAction {
        PREV,
        NEXT
    }

    val items = mutableListOf<ItemStack?>()
    var currentPage: Int = 1 // 1 based index
    protected var isVisible: Boolean = false
    protected var firstVisibleItem: Int = 0

    val totalPages: Int
        get() {
            val count = items.size
            return if (items.size <= size) 1 else (count + size - 5) / (size - 2)
        }

    fun nextPage() {
        currentPage++
        goTo(currentPage)
    }

    fun prevPage() {
        currentPage--
        goTo(currentPage)
    }

    fun displayRow() {
        goTo(currentPage)
    }

    protected open fun goTo(page: Int) {
        var page = page
        page = min(page, totalPages)
        firstVisibleItem = 0
        if (totalPages == 1) { // alle itemsene fyller heile rada
            for (i in 0..<size) {
                val item = if (i < items.size) items[i] else null
                inventory.setItem(startSlot + i, item)
            }
            return
        }
        if (page == 1) { // rada fylles med første side bortsett fra en next knapp på slutten
            for (i in 0..<size - 1) {
                val item = if (i < items.size) items.get(i) else null
                inventory.setItem(startSlot + i, item)
            }
            inventory.setItem(startSlot + size - 1, ROW_NEXT)
            return
        }
        firstVisibleItem = size - 1 + (size - 2) * (page - 2) // første element på den sida
        setItem(0, ROW_PREV) // en prev knapp først, deretter fylles rada opp bortsett fra den siste hvis det er en midtside, da blir det en next på slutten
        for (i in 0..<size - 2) {
            val targetSlot = 1 + i // begynner på slot 2 pga nr 1 er for PREV knappen
            val item = if (firstVisibleItem + i < items.size) items[firstVisibleItem + i] else null
            setItem(targetSlot, item)
        }
        if (page == totalPages) {
            val item = if (firstVisibleItem + size - 2 < items.size) items[firstVisibleItem + size - 2] else null // size-2: size er 1 indexed, så -1, og første slot er opptatt (prev knapp), så -1 igjen
            setItem(size - 1, item)
        } else {
            setItem(size - 1, ROW_NEXT)
        }
    }

    fun handleClick(e: InventoryClickEvent): Boolean {
        if (!isVisible) return false

        val slot = e.slot
        if (slot < startSlot || slot >= startSlot + size) { // check if the clicked item was inside this sliders space, since its possible to have many sliders on a page
            return false
        }
        val item = e.getCurrentItem() ?: return false
        if (!item.hasItemMeta()) return false

        val actionString = item.itemMeta.persistentDataContainer // check if it has a row action tag = its a button (prev/next)
            .get(KEY_ROW_ACTION, PersistentDataType.STRING) ?: return false

        val action = RowAction.valueOf(actionString)

        if (action == RowAction.PREV) {
            prevPage()
        } else if (action == RowAction.NEXT) {
            nextPage()
        }
        return true
    }

    private fun setItem(index: Int, item: ItemStack?) {
        inventory.setItem(startSlot + index, item)
    }

    open fun addItem(item: ItemStack) {
        if (menuActionId != null) item.editMeta {
            it.persistentDataContainer.set(Menu.ACTION_KEY, PersistentDataType.STRING, menuActionId)
        }
        items.add(item)
        if (isVisible && currentPage == totalPages) {
            displayRow()
        }
    }

    fun removeItem(item: ItemStack?) {
        items.remove(item)
        if (isVisible) displayRow()
    }

    fun show() {
        isVisible = true
        displayRow()
    }

    fun hide() {
        isVisible = false
    } // xxxxxx
    // 111122
    // xxxxxx--     xxxxxxxx
    // 111222       11112222
    // xxxxxxx--    xxxxxxxxx
    // 111222333    111122233
    // xxxxxxxxxxx
    // 11112222

    companion object {
        val KEY_ROW_ACTION: NamespacedKey = NamespacedKey("botbows", "row_action")

        private val ROW_PREV: ItemStack =
            Menu.makeItem("prev", Component.text("Prev"), KEY_ROW_ACTION, RowAction.PREV.name)
        private val ROW_NEXT: ItemStack =
            Menu.makeItem("next", Component.text("Next"), KEY_ROW_ACTION, RowAction.NEXT.name)
    }
}
