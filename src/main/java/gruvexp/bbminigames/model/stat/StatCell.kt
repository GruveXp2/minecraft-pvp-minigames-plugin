package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCell(component: TextComponent, loc: Location, val layoutWidth: Float, parent: StatElement, layoutX: Float, layoutY: Float):
    StatElement(parent, layoutX, layoutY) {

    override fun positionX() {
        bgDisplay.apply { transformation = transformation.apply { translation.x = X_*layoutWidth + absoluteX + offsetX } }
        statDisplay.apply { transformation = transformation.apply { translation.x = X + absoluteX + offsetX } }
    }

    override fun positionY() {
        bgDisplay.apply { transformation = transformation.apply { translation.y = absoluteY + offsetY } }
        statDisplay.apply { transformation = transformation.apply { translation.y = absoluteY + offsetY } }
    }

    private val bgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        transformation = transformation.apply {
            scale.set(layoutWidth / textWidth(" "), 1f, 1f)
            translation.set(X_*layoutWidth + absoluteX, absoluteY, 0.01f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val statDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(component)
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X + absoluteX, absoluteY, 0.02f) }
        billboard = Display.Billboard.VERTICAL
    }

    var offsetX: Float = 0f
        set(value) {
            field = value
            positionX()
        }

    var offsetY: Float = 0f
        set(value) {
            field = value
            positionY()
        }

    fun setInvisible(invisible: Boolean) {
        if (invisible) {
            bgDisplay.apply { backgroundColor = Color.fromARGB(0) }
            statDisplay.apply { textOpacity = 0 }
        } else {
            bgDisplay.apply { backgroundColor = Color.fromARGB(0x40000000) }
            statDisplay.apply { textOpacity = 0xff.toByte() }
        }
    }

    fun remove() {
        bgDisplay.remove()
        statDisplay.remove()
    }
}