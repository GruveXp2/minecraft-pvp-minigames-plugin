package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatTab(val tabName: String, val loc: Location, val cols: List<StatCol>) {
    val isExpanded: Boolean = false

    private val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        val width = cols.sumOf { it.width.toDouble() }.toFloat()
        transformation = transformation.apply {
            scale.set(width, 1f, 1f);
            translation.set(X*width, 0f, 0f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val displays: Set<TextDisplay> = setOf(headerBgDisplay, headerDisplay)

    val width: Float = textWidth(tabName)

    fun setPlayerHeight(bp: BotBowsPlayer) {

    }

    fun recalculateHeight() {

    }

    fun setOffset(offset: Float) {
        headerBgDisplay.apply {
            transformation = transformation.apply { translation.set(X*width + offset, 0.0, 0.0) }
        }
        headerDisplay.apply {
            transformation = transformation.apply { translation.set(X + offset, 0f, 0f) }
        }
    }
}