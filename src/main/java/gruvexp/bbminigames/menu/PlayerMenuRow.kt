package gruvexp.bbminigames.menu

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class PlayerMenuRow(inventory: Inventory, menuActionId: String, startSlot: Int, size: Int) :
    MenuRow(inventory, menuActionId, startSlot, size) {
    override fun addItem(item: ItemStack) {
        require(item.type == Material.PLAYER_HEAD) { "The item must be a player head" }
        super.addItem(item)
    }

    fun getItem(bp: BotBowsPlayer): ItemStack? {
        items.filterNotNull().forEach {
            val key = NamespacedKey(Main.getPlugin(), "uuid")
            val storedUUID = it.itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING)
            if (storedUUID == bp.avatar.uuid.toString()) {
                return@getItem it
            }
        }
        return null
    }

    fun editItem(bp: BotBowsPlayer, action: ItemMeta.() -> Unit) {
        val item = getItem(bp) ?: return
        item.editMeta(action)
        if (isVisible) displayRow()
    }

    fun removeItem(bp: BotBowsPlayer) {
        removeItem(getItem(bp))
    }
}
