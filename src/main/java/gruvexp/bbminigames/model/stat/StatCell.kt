package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCell(component: TextComponent, loc: Location, val layoutWidth: Float, layoutX: Float, layoutY: Double) {

    var layoutX: Float = layoutX
        set(value) {
            bgDisplay.apply { transformation = transformation.apply { translation.x = X_*layoutWidth + value + offsetX } }
            statDisplay.apply { transformation = transformation.apply { translation.x = X + value + offsetX } }
            field = value
        }

    private val bgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, layoutY, 0.0), TextDisplay::class.java).apply {
        text(Component.text(" "))
        transformation = transformation.apply {
            scale.set(layoutWidth / textWidth(" "), 1f, 1f)
            translation.set(X_*layoutWidth + layoutX, 0f, 0.01f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val statDisplay = Main.WORLD.spawn(loc.clone().add(0.0, layoutY, 0.0), TextDisplay::class.java).apply {
        text(component)
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X + layoutX, 0f, 0.02f) }
        billboard = Display.Billboard.VERTICAL
    }

    var offsetX: Float = 0f
        set(value) {
            bgDisplay.apply { transformation = transformation.apply { translation.x = X_*layoutWidth + layoutX + value } }
            statDisplay.apply { transformation = transformation.apply { translation.x = X + layoutX + value } }
            field = value
        }

    var offsetY: Float = 0f
        set(value) {
            bgDisplay.apply { transformation = transformation.apply { translation.y = value } }
            statDisplay.apply { transformation = transformation.apply { translation.y = value } }
            field = value
        }

    fun remove() {
        bgDisplay.remove()
        statDisplay.remove()
    }
}