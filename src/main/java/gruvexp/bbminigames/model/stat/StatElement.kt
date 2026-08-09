package gruvexp.bbminigames.model.stat

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

    fun updateX() { positionX(); children.forEach { it.updateX() } }
    fun updateY() { positionY(); children.forEach { it.updateY() } }

    protected abstract fun positionX()
    protected abstract fun positionY()
}