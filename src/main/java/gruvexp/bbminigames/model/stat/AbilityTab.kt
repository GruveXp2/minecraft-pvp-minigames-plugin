package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack

class AbilityTab(tabName: String, loc: Location, layoutY: Float, playerStats: List<PlayerMatchStats>, onExpandToggle: () -> Unit) : StatTab(tabName, loc, layoutY, onExpandToggle) {
    override val layoutWidth: Float
        get() {
            return rows.maxOf { it.layoutWidth }
        }

    val rows: List<AbilityRow> = playerStats.mapIndexed { index, stats ->
        AbilityRow(this, absoluteX, -HEIGHT_PX * index, stats)
    }.toList()

    override fun expand() {
        TODO("Not yet implemented")
    }

    override fun collapse() {
        TODO("Not yet implemented")
    }
}

class AbilityRow(parent: StatElement, layoutX: Float, layoutY: Float, playerStats: PlayerMatchStats) : StatElement(parent, layoutX, layoutY) {

    val layoutWidth: Float
        get() {
        }

    override fun initSelf() {
        TODO("Not yet implemented")
    }

    override fun positionX() {
        TODO("Not yet implemented")
    }

    override fun positionY() {
        TODO("Not yet implemented")
    }

}

const val ICON_SIZE = 0.25f

class AbilityCell(loc: Location, parent: StatElement, layoutX: Float, layoutY: Float, item: ItemStack, statComponent: TextComponent) : StatElement(parent, layoutX, layoutY) {

    var isExpanded: Boolean
        set(value) {

        }

    val layoutWidth: Float
        get() = ICON_SIZE + (if (isExpanded) 12*PX else 0f) + 2*PX

    private val bgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        transformation = transformation.apply {
            scale.set(layoutWidth / textWidth(" "), 0.9f, 1f)
            translation.set(X_*layoutWidth + absoluteX, absoluteY + PX, -0.01f)
        }
        billboard = Display.Billboard.VERTICAL
    }

    private val iconDisplay = Main.WORLD.spawn(loc, ItemDisplay::class.java).apply {
        setItemStack(item)
        transformation = transformation.apply { translation.set(X + absoluteX, absoluteY, 0.01f) }
        billboard = Display.Billboard.VERTICAL
    }

    private val statDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(statComponent)
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X + absoluteX, absoluteY, 0.01f) }
        billboard = Display.Billboard.VERTICAL
    }

    override fun initSelf() {
        bgDisplay.apply { interpolationDuration = ANIMATION_TICKS }
        iconDisplay.apply { interpolationDuration = ANIMATION_TICKS }
        statDisplay.apply { interpolationDuration = ANIMATION_TICKS }
    }

    override fun positionX() {
        bgDisplay.animate { translation.x = X_*layoutWidth + absoluteX }
        iconDisplay.animate { translation.x = X_*layoutWidth + absoluteX }
        statDisplay.animate(isExpanded) { translation.x = X + absoluteX + 10*PX }
    }

    override fun positionY() {
        bgDisplay.animate { translation.y = absoluteY }
        iconDisplay.animate { translation.y = absoluteY }
        statDisplay.animate(isExpanded) { translation.y = absoluteY }
    }

}