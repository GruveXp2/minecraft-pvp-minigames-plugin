package gruvexp.bbminigames.menu

import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import kotlin.math.min

open class MenuSlider(
    protected val inventory: Inventory,
    protected val menuActionId: String?,
    var startSlot: Int,
    protected val filledTrackMaterial: Material,
    protected val filledTrackColor: NamedTextColor,
    protected val sliderSteps: List<String>,
    protected val description: String
) {

    fun setProgressSlots(slots: Int) {
        var slots = slots
        slots = min(slots, sliderSteps.size) // Begrenser slots til sliderens størrelse
        for (i in sliderSteps.indices) {
            val item = if (i < slots)
                Menu.makeItem(
                    filledTrackMaterial,
                    Component.text(sliderSteps[i], filledTrackColor),
                    menuActionId,
                    Component.text(description)
                )
            else
                Menu.makeItem(
                    EMPTY_TRACK_MATERIAL,
                    Component.text(sliderSteps[i], EMPTY_TRACK_COLOR),
                    menuActionId,
                    Component.text(description)
                )
            inventory.setItem(i + startSlot, item)
        }
    }

    fun setProgress(progressTick: String?) {
        if (progressTick !in sliderSteps) {
            setProgressSlots(0)
            BotBows.debugMessage("$progressTick Doesnt exist", TestCommand.verboseDebugging)
        }
        setProgressSlots(sliderSteps.indexOf(progressTick) + 1)
    }

    fun getNext(step: String): String {
        var i = sliderSteps.indexOf(step)
        //BotBows.debugMessage(String.format("current(%d): %s", i, sliderSteps.get(i)));
        i++
        if (i == sliderSteps.size) {
            i = 0
        }
        //BotBows.debugMessage(String.format("next(%d): %s", i, sliderSteps.get(i)));
        return sliderSteps[i]
    }

    companion object {
        private val EMPTY_TRACK_MATERIAL = Material.WHITE_STAINED_GLASS_PANE
        private val EMPTY_TRACK_COLOR: NamedTextColor = NamedTextColor.WHITE
    }
}
