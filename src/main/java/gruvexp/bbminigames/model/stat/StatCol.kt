package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCol(val tabName: String, val loc: Location, layoutY: Double, val format: (PlayerMatchStats) -> TextComponent, statList : List<PlayerMatchStats>) {
    val colWidth: Float
        get() {
            return textWidth(tabName) + 5*2*PX // 5px margin
        }

    var layoutX: Float = 0f
        set(value) {
            field = value
            updateX()
            cells.values.forEach { it.layoutX = value }
        }

    var offsetX: Float = 0f
        set(value) {
            field = value
            updateX()
            cells.values.forEach { it.offsetX = value }
        }

    private val headerBgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, layoutY, 0.0), TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply {
            scale.x = colWidth / textWidth(" ")
            translation.x = X_*colWidth + layoutX
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc.clone().add(0.0, layoutY, 0.0), TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X + layoutX, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val cells: Map<BotBowsPlayer, StatCell> = statList.withIndex().associate { (i, stats) ->
        stats.bp to StatCell(format(stats), loc, colWidth - 2*PX, layoutX, layoutY - (i + 1) * HEIGHT_PX) // 1px margin
    }

    fun setPlayerHeight(bp: BotBowsPlayer) {

    }

    fun recalculateHeight() {

    }

    fun updateX() {
        headerBgDisplay.apply { transformation = transformation.apply { translation.x = X_*colWidth + layoutX + offsetX } }
        headerDisplay.apply { transformation = transformation.apply { translation.x = X + layoutX + offsetX } }
    }

    fun remove() {
        headerBgDisplay.remove()
        headerDisplay.remove()
        cells.values.forEach { it.remove() }
    }
}