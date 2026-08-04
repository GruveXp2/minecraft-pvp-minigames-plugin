package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCol(val tabName: String, val loc: Location, val format: (PlayerMatchStats) -> TextComponent, statList : List<PlayerMatchStats>, x: Double) {

    val width: Float
        get() {
            return textWidth(tabName) // basért på tittel, og kanskje innholdet, kommer an på lissom
        }

    private val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply {
            scale.set(width, 1.0, 1.0);
            translation.set(X*width, 0.0, 0.0)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val cells: Map<BotBowsPlayer, StatCell> = statList.withIndex().associate { (i, stats) ->
        stats.bp to StatCell(format(stats), loc, textWidth(tabName) - 2 * PX, x, i * HEIGHT_PX)
    }

    fun setPlayerHeight(bp: BotBowsPlayer) {

    }

    fun recalculateHeight() {

    }

    fun setOffsetX(offset: Double) {
        headerBgDisplay.apply {
            transformation = transformation.apply { translation.set(X*width + offset, 0.0, 0.0) }
        }
        headerDisplay.apply {
            transformation = transformation.apply { translation.set(X + offset, 0.0, 0.0) }
        }
        cells.values.forEach { it.offsetX = offset }
    }

    //TODO: kalkulering av y posisjon, at man setter det.
}