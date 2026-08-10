package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

abstract class StatTab(tabName: String, loc: Location, layoutY: Float, val onExpandToggle: () -> Unit): StatElement(null, 0f, layoutY) {
    var isExpanded: Boolean = false
        set(value) {
            field = value
            if (value) expand() else collapse()
        }

    abstract val layoutWidth: Float

    protected val headerBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
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
        headerBgDisplay.apply { interpolationDuration = ANIMATION_TICKS }
        headerDisplay.apply { interpolationDuration = ANIMATION_TICKS }
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

    protected abstract fun expand()

    protected abstract fun collapse()

    open fun remove() {
        displays.forEach { it.remove() }
    }
}