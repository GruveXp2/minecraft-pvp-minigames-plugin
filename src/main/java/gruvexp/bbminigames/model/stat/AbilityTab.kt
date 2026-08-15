package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

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

    private var expandTask: BukkitTask? = null

    override fun expand() {
        rows.forEach { it.isExpanded = true }
        calculateRowPlacement()
        expandTask?.cancel()
        onExpandToggle()
    }

    override fun collapse() {
        rows.forEach { it.isExpanded = false }
        calculateRowPlacement()
        expandTask?.cancel()
        expandTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (!isExpanded) {
                onExpandToggle()
            }
        }, ANIMATION_TICKS.toLong() + 1)
    }

    fun calculateRowPlacement() { // offset them so they are left aligned instead of center (maybe replace layoutXY with leftaligned as default, and provide functions to calculate center and right aligned with the layoutWidth?)
        val newX = -layoutWidth / 2
        rows.forEach { it.layoutX = newX }
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

    val crossbowCell = AbilityCell(loc, this, 0f, 0f, BotBows.BOTBOW, Component.text(playerStats.crossbowKills, NamedTextColor.RED))
        .apply { isExpanded = true; isHidden = true }
        .also { children.add(it) }
    val abilityCells = playerStats.abilitySuccesses
        .map { (abilityType, successes) -> AbilityCell(loc, this, 0f, 0f, abilityType.abilityItem, Component.text(successes)) }
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
        var totalWidth = if (isExpanded) crossbowCell.layoutWidth + 2*PX else 0f
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

const val ICON_SIZE = 0.25f

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

    private var expandTask: BukkitTask? = null

    private fun expand() {
        updateX()
        bgDisplay.animate { scale.set(layoutWidth / textWidth(" ")) }

        expandTask?.cancel()
        expandTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (isExpanded) {
                statDisplay.animate { scale.set(1f) }
            }
        }, ANIMATION_TICKS.toLong() + 1)
    }

    private fun collapse() {
        statDisplay.animate { scale.set(0f) }

        expandTask?.cancel()
        expandTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (!isExpanded) {
                updateX()
                bgDisplay.animate { scale.set(layoutWidth / textWidth(" ")) }
            }
        }, ANIMATION_TICKS.toLong() + 1)
    }

    private fun show() {
        BotBows.debugMessage("scale -> 8*PX", debug)
        bgDisplay.animate { scale.x = layoutWidth / textWidth(" ") }
        iconDisplay.animate { scale.set(8*PX) }
        if (isExpanded) expand()
    }

    private fun hide() {
        BotBows.debugMessage("scale -> 0", debug)
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
            translation.set(X + absoluteX, absoluteY, 0.01f)
            scale.set(8*PX)
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
        iconDisplay.animate { translation.x = X_*layoutWidth + absoluteX }
        statDisplay.animate(isExpanded) { translation.x = X + absoluteX + 10*PX }
    }

    override fun positionY() {
        bgDisplay.animate { translation.y = absoluteY }
        iconDisplay.animate { translation.y = absoluteY }
        statDisplay.animate(isExpanded) { translation.y = absoluteY }
    }

    fun remove() {
        bgDisplay.remove()
        iconDisplay.remove()
        statDisplay.remove()
    }
}