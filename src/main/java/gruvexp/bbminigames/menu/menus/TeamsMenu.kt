package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class TeamsMenu(settings: Settings) : SettingsMenu(settings) {
    lateinit var team1: BotBowsTeam
    lateinit var team2: BotBowsTeam

    init {
        for (i in 2..6) {
            inventory.setItem(i, null)
            inventory.setItem(i + 9, null)
        }
        setPageButtons(2, true, true)
        inventory.setItem(22, SWITCH_SIDE)
        registerTeams()
    }

    override fun getMenuName(): Component = Component.text("Teams (2/6)")

    override fun getSlots(): Int = 27

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return

        val clicker = e.whoClicked as Player
        if (!settings.checkMod(settings.lobby.getBotBowsPlayer(clicker))) return

        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        when (action) {
            MenuAction.FLIP_PLAYER_TEAM -> {
                val key = NamespacedKey(Main.getPlugin(), "uuid")
                val playerId = UUID.fromString(
                    clickedItem.itemMeta.persistentDataContainer
                        .get(key, PersistentDataType.STRING)
                )
                val headBp = settings.lobby.getBotBowsPlayer(playerId)
                headBp.team.oppositeTeam!!.join(headBp)
                recalculateTeam()
                settings.healthMenu.updateColors() // updates colors of player head displaynames in other menus
            }
            MenuAction.FLIP_TEAMS -> settings.switchTeamSides()
        }
    }

    public override fun prevPage(p: Player) {
        settings.mapMenus[BotBows.getBotBowsPlayer(p)]!!.open(p)
    }

    public override fun nextPage(p: Player) {
        settings.healthMenu.open(p)
    }

    fun registerTeams() {
        team1 = settings.team1
        team2 = settings.team2
        drawTeamGlassPanes()
    }

    private fun drawTeamGlassPanes() { // update the glass pane items that show the team colors and name
        val team1Pane = makeItem(team1.glassPane, Component.text("Team ${team1.displayName}", team1.color))
        val team2Pane = makeItem(team2.glassPane, Component.text("Team ${team2.displayName}", team2.color))
        inventory.setItem(0, team1Pane)
        inventory.setItem(1, team1Pane)
        inventory.setItem(7, team1Pane)
        inventory.setItem(8, team1Pane)
        inventory.setItem(9, team2Pane)
        inventory.setItem(10, team2Pane)
        inventory.setItem(16, team2Pane)
        inventory.setItem(17, team2Pane)
    }

    fun recalculateTeam() { // TODO: use menurows instead
        inventory.remove(Material.PLAYER_HEAD) // Fjerner player heads sånn at det kan kalkuleres pånytt

        for (i in 0..<team1.size()) { // team 1
            val playerHead = team1.getPlayer(i).avatar.getHeadItem().apply { editMeta {
                it.persistentDataContainer.set(ACTION_KEY, PersistentDataType.STRING, MenuAction.FLIP_PLAYER_TEAM.name)
            } }
            inventory.setItem(2 + i, playerHead)
        }
        for (i in 0..<team2.size()) { // team 2
            val playerHead = team1.getPlayer(i).avatar.getHeadItem().apply { editMeta {
                it.persistentDataContainer.set(ACTION_KEY, PersistentDataType.STRING, MenuAction.FLIP_PLAYER_TEAM.name)
            } }
            inventory.setItem(11 + i, playerHead)
        }
    }

    companion object {
        val SWITCH_SIDE: ItemStack = makeItem(
            "switch",
            Component.text("Switch sides", NamedTextColor.LIGHT_PURPLE),
            MenuAction.FLIP_TEAMS.name,
            Component.text("switches the teams to be their other"),
            Component.text("so the teams spawn on the opposite side")
        )
    }

    private enum class MenuAction {
        FLIP_PLAYER_TEAM,
        FLIP_TEAMS
    }
}
