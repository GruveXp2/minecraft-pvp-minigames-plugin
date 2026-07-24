package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.model.preset.BattlePreset
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Settings
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class PresetsMenu(settings: Settings): SettingsMenu(settings) {

    init {
        setPageButtons(1, false, true)
        displayPresets()
    }

    override fun getMenuName(): Component = Component.text("Presets (0/6)")
    override fun getSlots(): Int = 18

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return
        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker) ?: return
        if (!settings.checkMod(bp)) return

        val presetName = getActionId(clickedItem) ?: return
        val preset: BattlePreset = Main.getPlugin().presetService.getPreset(presetName)!!
        bp.lobby.settings.applyBattlePreset(preset)
    }

    fun displayPresets() {
        val presets = Main.getPlugin().presetService.getPresets()

        for (i in presets.size..slots - 9) {
            inventory.setItem(i, VOID)
        }
        presets.forEachIndexed { index, preset ->
            val item = makeItem(preset.icon, Component.text(preset.name), preset.name)
            inventory.setItem(index, item)
        }
    }

    override fun nextPage(p: Player) = settings.mapMenus[BotBows.getBotBowsPlayer(p)]!!.open(p)
}