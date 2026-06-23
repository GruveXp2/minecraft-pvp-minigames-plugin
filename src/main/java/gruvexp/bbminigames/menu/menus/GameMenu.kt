package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.sumo.SumoManager
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class GameMenu : Menu() {
    init {
        inventory.setItem(3, BOTBOWS)
        inventory.setItem(5, SUMO)
    }

    override fun getMenuName(): Component = Component.text("Game Menu")
    override fun getSlots(): Int = 9

    override fun handleMenu(e: InventoryClickEvent) {
        val p = e.whoClicked as Player
        val clickedItem = e.currentItem ?: return

        val actionId =
            clickedItem.persistentDataContainer.get(ACTION_KEY, PersistentDataType.STRING) ?: return

        val action = MenuAction.valueOf(actionId)
        when (action) {
            MenuAction.JOIN_BOTBOWS -> BotBows.lobbyMenu.open(p)
            MenuAction.JOIN_SUMO -> SumoManager.sumoMenu.open(p)
        }
    }

    companion object {
        val BOTBOWS: ItemStack = makeItem(
            Material.BOW,
            MenuAction.JOIN_BOTBOWS.name,
            Component.text("BotBows Classic"),
            Component.text("The classic game of BotBows")
        )
        val SUMO: ItemStack = makeItem(
            Material.STICK,
            MenuAction.JOIN_SUMO.name,
            Component.text("Sumo"),
            Component.text("Knockback pvp with sticks")
        )
    }

    private enum class MenuAction {
        JOIN_BOTBOWS,
        JOIN_SUMO,
    }
}
