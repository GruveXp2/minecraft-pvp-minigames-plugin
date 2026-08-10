package gruvexp.bbminigames.model.stat

import org.bukkit.entity.Display
import org.bukkit.util.Transformation

const val ANIMATION_TICKS = 3

// animated = false skips the trigger entirely, so the display jumps straight to the new transformation.
// Needed for invisible displays: restarting the clock also restarts every other field that changed since
// the last restart, which rewinds a finished fade back to where it started
fun Display.animate(animated: Boolean = true, delay: Int = 0, block: Transformation.() -> Unit) {
    if (animated) interpolationDelay = delay // this is a trigger camouflaged as a field, so have to set it to 0 every time to trigger the animation, even tho it was already 0
    transformation = transformation.apply(block)
}

abstract class StatElement(val parent: StatElement?, layoutX: Float, layoutY: Float) {

    val children: MutableSet<StatElement> = mutableSetOf()

    val absoluteX: Float get() = (parent?.absoluteX ?: 0f) + layoutX
    val absoluteY: Float get() = (parent?.absoluteY ?: 0f) + layoutY

    var layoutX: Float = layoutX
        set(value) {
            field = value
            updateX()
        }

    var layoutY: Float = layoutY
        set(value) {
            field = value
            updateY()
        }

    private var inited = false
    fun init() {
        if (inited) return
        inited = true
        initSelf()
        children.forEach { it.init() }
    }
    protected abstract fun initSelf()

    fun updateX() { positionX(); children.forEach { it.updateX() } }
    fun updateY() { positionY(); children.forEach { it.updateY() } }

    protected abstract fun positionX()
    protected abstract fun positionY()
}