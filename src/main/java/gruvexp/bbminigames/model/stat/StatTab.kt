package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatTab(val tabName: String, val loc: Location, val yPos: Float, val cols: List<StatCol>) {
    val isExpanded: Boolean = false

    val layoutWidth = cols.sumOf { it.colWidth.toDouble() }.toFloat()

    private val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply {
            scale.set(layoutWidth / textWidth(" "), 1f, 1f)
            translation.set(X_*layoutWidth, yPos, 0f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, yPos, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val displays: Set<TextDisplay> = setOf(headerBgDisplay, headerDisplay)

    init {
        val totalWidth = cols.sumOf { it.colWidth.toDouble() }
        var x = - totalWidth / 2
        cols.forEach {
            x += it.colWidth/2
            it.layoutX = x.toFloat()
            x += it.colWidth/2
        }
    }

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