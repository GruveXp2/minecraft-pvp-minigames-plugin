package dev.gruve.minigames.menu.menus

import dev.gruve.minigames.Main
import dev.gruve.minigames.menu.SettingsMenu
import dev.gruve.minigames.botbows.preset.BattlePreset
import dev.gruve.minigames.botbows.BotBows
import dev.gruve.minigames.botbows.Settings
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class PresetsMenu(settings: Settings) : SettingsMenu(settings, Component.text("Presets (0/6)"), 18) {

    init {
        setPageButtons(1, prevMenuButton = false, nextMenuButton = true)
        displayPresets()
    }

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return
        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker) ?: return
        if (!settings.checkMod(bp)) return

        val presetName = getActionId(clickedItem) ?: return
        val preset: BattlePreset = Main.plugin.presetService.getPreset(presetName)!!
        bp.lobby.settings.applyBattlePreset(preset)
    }

    fun displayPresets() {
        val presets = Main.plugin.presetService.getPresets()

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