package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.settings.WinConditionUpdateListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class WinConditionMenu(settings: Settings?) : SettingsMenu(settings), WinConditionUpdateListener {
    init {
        setPageButtons(2, true, true)

        inventory.setItem(2, SUB_10)
        inventory.setItem(3, SUB_1)
        inventory.setItem(5, ADD_1)
        inventory.setItem(6, ADD_10)

        inventory.setItem(11, SUB_10)
        inventory.setItem(12, SUB_1)
        inventory.setItem(14, ADD_1)
        inventory.setItem(15, ADD_10)

        onWinScoreThresholdChange()
        onRoundDurationChange()
        onDynamicScoreToggle()
    }

    override fun getMenuName(): Component {
        return Component.text("Win condition (4/6)")
    }

    override fun getSlots(): Int {
        return 27
    }

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return
        val clicker = e.whoClicked as Player
        if (!settings.checkMod(settings.lobby.getBotBowsPlayer(clicker))) return
        val slot = e.slot

        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        val winConditionSettings = settings.winConditionSettings
        when (action) {
            MenuAction.TOGGLE_DYNAMIC_POINTS ->
                winConditionSettings.isDynamicScoring = !winConditionSettings.isDynamicScoring
            MenuAction.SUB_10 -> {
                if (slot < 9) {
                    changeWinScoreThreshold(-10)
                } else {
                    changeRoundDuration(-10)
                }
            }
            MenuAction.SUB_1 -> {
                if (slot < 9) {
                    changeWinScoreThreshold(-1)
                } else {
                    changeRoundDuration(-1)
                }
            }
            MenuAction.ADD_1 -> {
                if (slot < 9) {
                    changeWinScoreThreshold(1)
                } else {
                    changeRoundDuration(1)
                }
            }
            MenuAction.ADD_10 -> {
                if (slot < 9) {
                    changeWinScoreThreshold(10)
                } else {
                    changeRoundDuration(10)
                }
            }
        }
    }

    private fun changeWinScoreThreshold(Δthreshold: Int) {
        val winConditionSettings = settings.winConditionSettings
        winConditionSettings.winScoreThreshold += Δthreshold
    }

    private fun changeRoundDuration(Δduration: Int) {
        val winConditionSettings = settings.winConditionSettings
        winConditionSettings.roundDuration += Δduration
    }

    public override fun prevPage(p: Player) {
        settings.healthMenu.open(p)
    }

    public override fun nextPage(p: Player) {
        settings.hazardMenu.open(p)
    }

    override fun onWinScoreThresholdChange() {
        val threshold = settings.winConditionSettings.winScoreThreshold

        val item = if (threshold > 0) {
            makeItem(
                Material.BLUE_TERRACOTTA,
                Component.text("Win score threshold", NamedTextColor.BLUE),
                Component.text(threshold, NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(" points to win", NamedTextColor.DARK_PURPLE))
            ).apply { amount = threshold }
        } else {
            makeItem(
                Material.YELLOW_TERRACOTTA,
                Component.text("Infinite rounds", NamedTextColor.YELLOW),
                Component.text("Run /stopgame to stop the game")
            )
        }
        inventory.setItem(4, item)
    }

    override fun onRoundDurationChange() {
        val roundDuration = settings.winConditionSettings.roundDuration

        val item = if (roundDuration > 0) {
            makeItem(
                Material.BLUE_TERRACOTTA,
                Component.text("Round duration", NamedTextColor.BLUE),
                Component.text("$roundDuration min", NamedTextColor.LIGHT_PURPLE)
            ).apply { amount = roundDuration }
        } else {
            makeItem(
                Material.YELLOW_TERRACOTTA,
                Component.text("No timer", NamedTextColor.YELLOW),
                Component.text("Run /stopgame to stop the game")
            )
        }
        inventory.setItem(13, item)
    }

    override fun onDynamicScoreToggle() {
        if (settings.winConditionSettings.isDynamicScoring) {
            inventory.setItem(8, DYNAMIC_POINTS_ENABLED)
        } else {
            inventory.setItem(8, DYNAMIC_POINTS_DISABLED)
        }
    }

    companion object {
        private val DYNAMIC_POINTS_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Dynamic points", NamedTextColor.RED),
            MenuAction.TOGGLE_DYNAMIC_POINTS.name,
            STATUS_DISABLED,
            Component.text("If enabled, winning team gets 1"),
            Component.text("point for each remaining hp."),
            Component.text("If disbabled, winning team only gets 1 point.")
        )

        private val DYNAMIC_POINTS_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Dynamic points", NamedTextColor.GREEN),
            MenuAction.TOGGLE_DYNAMIC_POINTS.name,
            STATUS_ENABLED,
            Component.text("If enabled, winning team gets 1"),
            Component.text("point for each remaining hp."),
            Component.text("If disbabled, winning team only gets 1 point.")
        )

        private val SUB_10 = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("-10"), MenuAction.SUB_10.name)
        private val SUB_1  = makeItem(Material.PINK_STAINED_GLASS_PANE, Component.text("-1"), MenuAction.SUB_1.name)
        private val ADD_1  = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("+1"), MenuAction.ADD_1.name)
        private val ADD_10 = makeItem(Material.GREEN_STAINED_GLASS_PANE, Component.text("+10"), MenuAction.ADD_10.name)

        private enum class MenuAction {
            ADD_1,
            ADD_10,
            SUB_1,
            SUB_10,
            TOGGLE_DYNAMIC_POINTS
        }
    }
}
