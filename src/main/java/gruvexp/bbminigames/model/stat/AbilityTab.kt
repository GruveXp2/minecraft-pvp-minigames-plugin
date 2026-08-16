package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.joml.AxisAngle4f

class AbilityTab(tabName: String, loc: Location, layoutY: Float, playerStats: List<PlayerMatchStats>, onExpandToggle: () -> Unit) : StatTab(tabName, loc, layoutY, onExpandToggle) {
    override val layoutWidth: Float
        get() {
            return rows.maxOf { it.layoutWidth }
        }

    val rows: List<AbilityRow> = playerStats
        .mapIndexed { index, stats -> AbilityRow(loc, this, 0f, -HEIGHT_PX * (index + 2), stats) }
        .toList()
        .also { children.addAll(it) }

    override fun initSelf() {
        super.initSelf()
        calculateRowPlacement()
    }

    override fun expand() {
        rows.forEach { it.isExpanded = true }
        calculateRowPlacement()
        onExpandToggle()
    }

    override fun collapse() {
        rows.forEach { it.isExpanded = false }
        calculateRowPlacement()
        onExpandToggle()
    }

    fun calculateRowPlacement() { // offset them so they are left aligned instead of center TODO: (replace with Alignment enum, LEFT, CENTER, RIGHT, that does it automatically)
        rows.forEach {
            val offset = if (it.isExpanded) {
                it.crossbowCell.layoutWidth / 2
            } else {
                (it.abilityCells.firstOrNull()?.layoutWidth ?: 0f) / 2
            }
            it.layoutX = -layoutWidth / 2 + offset
        }
    }

    override fun remove() {
        super.remove()
        rows.forEach { it.remove() }
    }
}

class AbilityRow(loc: Location, parent: StatElement, layoutX: Float, layoutY: Float, playerStats: PlayerMatchStats) : StatElement(parent, layoutX, layoutY) {

    var isExpanded: Boolean = false
        set(value) {
            field = value
            if (value) expand() else collapse()
        }

    var totalWidthCache = 0f
    val layoutWidth: Float
        get() {
            if (totalWidthCache == 0f) calculateCellPlacements()
            return totalWidthCache
        }

    val crossbowCell = AbilityCell(loc, this, 0f, 0f, BotBows.BOTBOW, Component.text(playerStats.crossbowHits, NamedTextColor.RED))
        .apply { isExpanded = true; isHidden = true }
        .also { children.add(it) }
    val abilityCells = playerStats.abilitySuccesses
        .map { (abilityType, successes) -> AbilityCell(loc, this, 0f, 0f, abilityType.abilityItem, Component.text(successes, abilityType.effect.color)) }
        .also { cells -> cells.forEach { it.isExpanded = false } }
        .also { children.addAll(it) }

    override fun initSelf() {
        calculateCellPlacements()
    }

    override fun positionX() {
        // not needed since theres no display elements
    }

    override fun positionY() {
        // ditto
    }

    private fun expand() {
        crossbowCell.isHidden = false
        abilityCells.forEach { it.isExpanded = true; it.updateX() }
        calculateCellPlacements()
    }

    private fun collapse() {
        crossbowCell.isHidden = true
        abilityCells.forEach { it.isExpanded = false; it.updateX() }
        calculateCellPlacements()
    }

    fun calculateCellPlacements() {
        var totalWidth = 1*PX + if (isExpanded) crossbowCell.layoutWidth + 2*PX else 0f
        abilityCells.forEach { cell ->
            cell.layoutX = totalWidth
            totalWidth += cell.layoutWidth + 2*PX
        }
        totalWidthCache = totalWidth
    }

    fun remove() {
        crossbowCell.remove()
        abilityCells.forEach { it.remove() }
    }
}

const val ICON_SIZE = 10*PX

class AbilityCell(loc: Location, parent: StatElement, layoutX: Float, layoutY: Float, item: ItemStack, statComponent: TextComponent) : StatElement(parent, layoutX, layoutY) {

    var isExpanded: Boolean = false
        set(value) {
            field = value
            if (!isHidden) {
                if (value) expand() else collapse()
            }
        }

    var isHidden: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) hide() else show()
        }

    val layoutWidth: Float
        get() = if (isHidden) 0f
                else ICON_SIZE + (if (isExpanded) 12*PX else 0f) + 2*PX

    private fun expand() {
        updateX()
        bgDisplay.animate { scale.x = layoutWidth / textWidth(" ") }
        statDisplay.animate { scale.set(1f) }
    }

    private fun collapse() {
        updateX()
        statDisplay.animate { scale.set(0f) }
        bgDisplay.animate { scale.x = layoutWidth / textWidth(" ") }
    }

    private fun show() {
        bgDisplay.animate { scale.x = layoutWidth / textWidth(" ") }
        iconDisplay.animate { scale.set(8*PX) }
        if (isExpanded) expand()
    }

    private fun hide() {
        bgDisplay.animate { scale.x = 0f }
        iconDisplay.animate { scale.set(0f) }
        if (isExpanded) collapse()
    }

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
        transformation = transformation.apply {
            translation.set(X + absoluteX, absoluteY + 6*PX, 0.01f)
            scale.set(8*PX)
            leftRotation.set(AxisAngle4f(Math.toRadians(180.0).toFloat(), 0f, 1f, 0f))
        }
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
        iconDisplay.animate { translation.x = absoluteX + (if (isExpanded) - 6*PX else 0f) }
        statDisplay.animate(isExpanded) { translation.x = X + absoluteX + 4*PX }
    }

    override fun positionY() {
        bgDisplay.animate { translation.y = absoluteY }
        iconDisplay.animate { translation.y = absoluteY + 6*PX }
        statDisplay.animate(isExpanded) { translation.y = absoluteY }
    }

    fun remove() {
        bgDisplay.remove()
        iconDisplay.remove()
        statDisplay.remove()
    }
}