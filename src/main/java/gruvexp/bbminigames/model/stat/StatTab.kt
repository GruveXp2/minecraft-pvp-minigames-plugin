package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatTab(tabName: String, loc: Location, layoutY: Float, val onExpandToggle: () -> Unit): StatElement(null, 0f, layoutY) {
    var isExpanded: Boolean = false
        set(value) {
            field = value
            hiddenCols.forEach { it.setInvisible(!value) }
            recalculateColumns()
            onExpandToggle()
        }

    val cols: MutableList<StatCol> = mutableListOf()
    val hiddenCols: MutableList<StatCol> = mutableListOf()

    val layoutWidth
        get() = cols.filter { it !in hiddenCols || isExpanded }.sumOf { it.colWidth.toDouble() }.toFloat()

    private val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply {
            scale.set(layoutWidth / textWidth(" "), 1f, 1f)
            translation.set(X_*layoutWidth, layoutY, 0f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, layoutY, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val displays: Set<TextDisplay> = setOf(headerBgDisplay, headerDisplay)

    override fun initSelf() {
        headerBgDisplay.apply { interpolationDuration = ANIMATION_TICKS }
        headerDisplay.apply { interpolationDuration = ANIMATION_TICKS }
    }

    override fun positionX() {
        headerBgDisplay.animate { translation.x = X_*layoutWidth + absoluteX }
        headerDisplay.animate { translation.x = X + absoluteX }
    }

    override fun positionY() {
        headerBgDisplay.animate { translation.y = absoluteY }
        headerDisplay.animate { translation.y = absoluteY }
    }

    fun addColumns(cols: List<StatCol>) {
        this.cols.addAll(cols)
        children.addAll(cols)
        recalculateColumns()
    }

    fun addHiddenColumns(cols: List<StatCol>) {
        hiddenCols.addAll(cols)
        addColumns(cols)
        if (!isExpanded) {
            cols.forEach { it.setInvisible(true) }
        }
    }

    fun recalculateColumns() {
        var x = - layoutWidth / 2
        cols.forEach {
            if (!hiddenCols.contains(it) || isExpanded) {
                x += it.colWidth/2
                it.layoutX = x
                x += it.colWidth/2
            }
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

    fun remove() {
        headerBgDisplay.remove()
        headerDisplay.remove()
        cols.forEach { it.remove() }
    }
}