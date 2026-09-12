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

    fun getAbilitySlot(type: AbilityType): Int {
        for (i in items.indices) {
            if (AbilityType.fromItem(items[i] ?: continue) == type) {
                var slot = i - firstVisibleItem
                if (currentPage > 1) slot++
                return slot
            }
        }
        error("missing ability in abilityRow")
    }

    override fun goTo(page: Int) {
        super.goTo(page)
        menu.updateAbilityStatuses()
        inventory.viewers
            .map { it as Player }
            .forEach { menu.open(it) }
    }
}
