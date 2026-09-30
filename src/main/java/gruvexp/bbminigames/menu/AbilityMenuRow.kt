package gruvexp.bbminigames.menu

import gruvexp.bbminigames.menu.menus.AbilityMenu
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class AbilityMenuRow(
    inventory: Inventory,
    menuActionId: String,
    startSlot: Int,
    size: Int,
    private val menu: AbilityMenu
) : MenuRow(inventory, menuActionId, startSlot, size) {
    init {
        AbilityType.entries.forEach { addItem(it.abilityItem.clone()) }
    }

    fun getAbilitySlot(type: AbilityType): Int? { // slot relative to the first visible element (excluding button)
        for (i in firstVisibleItem..<firstVisibleItem + getPageSize(currentPage)) {
            if (AbilityType.fromItem(items.getOrNull(i) ?: continue) != type) continue

            var slot = i - firstVisibleItem // index if firstVisibleItem.index = 0
            if (currentPage > 1) slot++ // ++ if not on first page, since then there will be a back button there and everything gets moved 1 to the right
            return slot
        }
        return null
    }

    override fun goTo(page: Int) {
        super.goTo(page)
        menu.updateAbilityStatuses()
        inventory.viewers
            .map { it as Player }
            .forEach { menu.open(it) }
    }
}
