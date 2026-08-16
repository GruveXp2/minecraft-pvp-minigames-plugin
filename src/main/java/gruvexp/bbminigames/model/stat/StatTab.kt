package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitTask

val HEADER_BG_COLOR = Color.fromARGB(100, 32, 50, 100)
val HEADER_BG_COLOR_HOVERED = Color.fromARGB(150, 48, 75, 150)

abstract class StatTab(tabName: String, loc: Location, layoutY: Float, val onExpandToggle: () -> Unit): StatElement(null, 0f, layoutY) {
    var isExpanded: Boolean = false
        set(value) {
            field = value
            if (value) expand() else collapse()
        }

    var isHovered: Boolean = false
        set(value) {
            hoverTask?.cancel()
            if (field == value) {
                if (field) scheduleDeHover()
                return
            }
            field = value
            if (value) hover() else deHover()
        }

    abstract val layoutWidth: Float

    protected val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = HEADER_BG_COLOR
        transformation = transformation.apply { translation.set(0f, layoutY, 0f) } // temp x translation, will get inited in updateX()
        billboard = Display.Billboard.VERTICAL
    }

    protected val headerDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(tabName))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, layoutY, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    private val displays: Set<TextDisplay> = setOf(headerBgDisplay, headerDisplay)

    override fun initSelf() {
        headerBgDisplay.interpolationDuration = ANIMATION_TICKS
        headerDisplay.interpolationDuration = ANIMATION_TICKS
    }

    override fun positionX() {
        headerBgDisplay.animate {
            translation.x = X_*layoutWidth + absoluteX
            scale.x = layoutWidth / textWidth(" ")
        }
        headerDisplay.animate { translation.x = X + absoluteX }
    }

    override fun positionY() {
        headerBgDisplay.animate { translation.y = absoluteY }
        headerDisplay.animate { translation.y = absoluteY }
    }

    var hoverTask: BukkitTask? = null

    protected abstract fun expand()

    protected abstract fun collapse()

    private fun hover() {
        headerBgDisplay.animate { translation.z = 2*PX }
        headerDisplay.animate { translation.z = 2*PX + 0.01f }
        headerBgDisplay.backgroundColor = HEADER_BG_COLOR_HOVERED
        scheduleDeHover()
    }

    private fun scheduleDeHover() {
        hoverTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { isHovered = false }, 2L)
    }

    private fun deHover() {
        headerBgDisplay.animate { translation.z = 0f }
        headerDisplay.animate { translation.z = 0.01f }
        headerBgDisplay.backgroundColor = HEADER_BG_COLOR
    }

    open fun remove() {
        displays.forEach { it.remove() }
    }
}