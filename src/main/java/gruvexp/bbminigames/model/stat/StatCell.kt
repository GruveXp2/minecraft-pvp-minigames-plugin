package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

class StatCell(component: TextComponent, loc: Location, width: Float, x: Double, y: Double) {
    private val bgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, y, 0.0), TextDisplay::class.java).apply {
        text(Component.text(" "))
        transformation = transformation.apply {
            scale.set(width, 1f, 1f)
            translation.set(X*width+ x, 0.0, 0.01)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val statDisplay = Main.WORLD.spawn(loc.clone().add(0.0, y, 0.0), TextDisplay::class.java).apply {
        text(component)
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X+ x, 0.0, 0.02) }
        billboard = Display.Billboard.VERTICAL
    }

    var offsetX: Double = 0.0
        set(value) {
            bgDisplay.apply {
                transformation = transformation.apply { translation.set(X * width + value, offsetY, 0.0) }
            }
            statDisplay.apply {
                transformation = transformation.apply { translation.set(X + value, offsetY, 0.0) }
            }
            field = value
        }

    var offsetY: Double = 0.0
        set(value) {
            bgDisplay.apply {
                transformation = transformation.apply { translation.set(X * width + offsetX, value, 0.0) }
            }
            statDisplay.apply {
                transformation = transformation.apply { translation.set(X + offsetX, value, 0.0) }
            }
            field = value
        }
}