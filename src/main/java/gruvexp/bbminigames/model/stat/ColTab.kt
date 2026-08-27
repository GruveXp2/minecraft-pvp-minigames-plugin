package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask

class ColTab(tabName: String, loc: Location, layoutY: Float, onExpandToggle: () -> Unit) : StatTab(tabName, loc, layoutY, onExpandToggle) {
    private var expandTask: BukkitTask? = null

    val cols: MutableList<StatCol> = mutableListOf()
    val hiddenCols: MutableList<StatCol> = mutableListOf()

    override val layoutWidth
        get() = cols.filter { it !in hiddenCols || isExpanded }.sumOf { it.colWidth.toDouble() }.toFloat()

    override fun expand() {
        if (hiddenCols.isEmpty()) return

        recalculateColumns()
        onExpandToggle()

        expandTask?.cancel()
        expandTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (isExpanded) {
                hiddenCols.forEach { it.setInvisible(false) }
            }
        }, ANIMATION_TICKS.toLong() + 1)
    }

    override fun collapse() {
        if (hiddenCols.isEmpty()) return

        hiddenCols.forEach { it.setInvisible(true) }

        expandTask?.cancel()
        expandTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (!isExpanded) {
                recalculateColumns()
                onExpandToggle()
            }
        }, ANIMATION_TICKS.toLong() + 1)
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
            if (it !in hiddenCols || isExpanded) {
                x += it.colWidth/2
                it.layoutX = x
                x += it.colWidth/2
            }
        }
    }

    override fun remove() {
        super.remove()
        cols.forEach { it.remove() }
    }
}