package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCol(val tabName: String, loc: Location, parent: StatElement, layoutY: Float, val format: (PlayerMatchStats) -> TextComponent, statList : List<PlayerMatchStats>):
    StatElement(parent, 0f, layoutY) {
    val colWidth: Float
        get() {
            return textWidth(tabName) + 5*2*PX // 5px margin
        }

    var offsetX: Float = 0f
        set(value) {
            field = value
            positionX()
            cells.values.forEach { it.offsetX = value }
        }

    private val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply {
            scale.x = colWidth / textWidth(" ")
            translation.set(X_*colWidth + absoluteX, absoluteY, 0f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X + absoluteX, absoluteY, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val cells: Map<BotBowsPlayer, StatCell> = statList.withIndex().associate { (i, stats) ->
        stats.bp to StatCell(format(stats), loc, colWidth - 2*PX, this, 0f, - (i + 1) * HEIGHT_PX) // 1px margin
    }.also { children.addAll(it.values) }

    fun setPlayerHeight(bp: BotBowsPlayer) {

    }

    fun recalculateHeight() {

    }

    override fun positionX() {
        headerBgDisplay.apply { transformation = transformation.apply { translation.x = X_*colWidth + absoluteX + offsetX } }
        headerDisplay.apply { transformation = transformation.apply { translation.x = X + absoluteX + offsetX } }
    }

    override fun positionY() {
        headerBgDisplay.apply { transformation = transformation.apply { translation.y = absoluteY } }
        headerDisplay.apply { transformation = transformation.apply { translation.y = absoluteY } }
    }

    fun remove() {
        headerBgDisplay.remove()
        headerDisplay.remove()
        cells.values.forEach { it.remove() }
    }
}