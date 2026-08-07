package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Lobby
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class LobbyMenu : Menu() {
    init {
        for (i in 0..2) {
            inventory.setItem(i, VOID)
            inventory.setItem(i + 6, VOID)
        }
    }

    override fun getMenuName(): Component = Component.text("Join Lobby")
    override fun getSlots(): Int = 9

    override fun handleMenu(e: InventoryClickEvent) {
        val clickedItem = e.getCurrentItem() ?: return
        val p = e.whoClicked as Player

        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        when (action) {
            MenuAction.JOIN_LOBBY -> {
                val displayName: Component = clickedItem.itemMeta.displayName() ?: return
                val text = PlainTextComponentSerializer.plainText().serialize(displayName)
                val lobbyID = text.substringAfter('#').trim().toInt() - 1
                BotBows.getLobby(lobbyID).joinGame(p)
            }
            MenuAction.FULL_LOBBY -> p.sendMessage(Component.text("Cant join lobby, lobby is full!", NamedTextColor.YELLOW))
            MenuAction.CLOSED_LOBBY -> p.sendMessage(Component.text("Cant join lobby, game is ongoing!", NamedTextColor.YELLOW))
        }
    }

    fun updateLobbyItem(lobby: Lobby) {
        val displayName = Component.text("Lobby #${lobby.id + 1}")
        val lobbyItem: ItemStack =
        if (lobby.isGameActive) {
            makeItem(
                Material.RED_CONCRETE, displayName, MenuAction.CLOSED_LOBBY.name, Component.text("Closed: active game")
            )
        } else {
            when (lobby.totalPlayers) {
                0 -> makeItem(
                    Material.LIGHT_GRAY_CONCRETE,
                    displayName,
                    MenuAction.JOIN_LOBBY.name,
                    Component.text("No players")
                )
                1, 2, 3, 4, 5 -> makeItem(
                    Material.LIME_CONCRETE,
                    displayName,
                    MenuAction.JOIN_LOBBY.name,
                    Component.text("${lobby.totalPlayers}/8 players")
                )
                6, 7 -> makeItem(
                    Material.YELLOW_CONCRETE,
                    displayName,
                    MenuAction.JOIN_LOBBY.name,
                    Component.text("${lobby.totalPlayers}/8 players")
                )
                else -> makeItem(
                    Material.ORANGE_CONCRETE,
                    displayName,
                    MenuAction.FULL_LOBBY.name,
                    Component.text("FULL")
                )
            }
        }
        inventory.setItem(3 + lobby.id, lobbyItem)
    }

    companion object {
        private enum class MenuAction {
            JOIN_LOBBY,
            FULL_LOBBY,
            CLOSED_LOBBY
        }
    }
}
