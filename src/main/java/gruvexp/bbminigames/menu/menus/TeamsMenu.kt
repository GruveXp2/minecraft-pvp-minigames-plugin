package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.PlayerListMenu
import gruvexp.bbminigames.menu.PlayerMenuRow
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import gruvexp.bbminigames.twtClassic.team.TeamSide
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class TeamsMenu(settings: Settings) : SettingsMenu(settings), PlayerListMenu {
    lateinit var team1: BotBowsTeam
    lateinit var team2: BotBowsTeam

    private val team1Row: PlayerMenuRow
    private val team2Row: PlayerMenuRow
    private val rows: Map<TeamSide, PlayerMenuRow>

    init {
        setPageButtons(2, true, true)
        inventory.setItem(22, SWITCH_SIDE)
        registerTeams()

        team1Row = PlayerMenuRow(inventory, MenuAction.FLIP_PLAYER_TEAM.name, 2, 5).apply { show() }
        team2Row = PlayerMenuRow(inventory, MenuAction.FLIP_PLAYER_TEAM.name, 11, 5).apply { show() }
        rows = mapOf(TeamSide.TEAM_1 to team1Row, TeamSide.TEAM_2 to team2Row)
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
                val clickedBp = settings.lobby.getBotBowsPlayer(playerId)
                settings.switchTeam(clickedBp)
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

    override fun addPlayer(bp: BotBowsPlayer) {
        rows.getValue(bp.team.teamSide).addItem(bp.avatar.getHeadItem())
    }

    override fun removePlayer(bp: BotBowsPlayer) {
        rows.getValue(bp.team.teamSide).removeItem(bp)
    }

    override fun updatePlayer(bp: BotBowsPlayer) {
        rows.getValue(bp.team.oppositeTeam.teamSide).removeItem(bp)
        rows.getValue(bp.team.teamSide).addItem(bp.avatar.getHeadItem())
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
