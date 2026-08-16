package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.joml.AxisAngle4f

class PlayerTab(val loc: Location, layoutY: Float, team1Players: List<BotBowsPlayer>, team2Players: List<BotBowsPlayer>) : StatElement(null, 0f, layoutY) {

    val textDisplays: MutableList<TextDisplay> = mutableListOf()
    val headDisplays: MutableList<ItemDisplay> = mutableListOf()

    init {
        (team1Players + team2Players).forEachIndexed { index, bp -> createPlayerRow(bp, -(index + 2) * HEIGHT_PX) }
    }

    override fun initSelf() {
        textDisplays.forEach { it.interpolationDuration = ANIMATION_TICKS }
        headDisplays.forEach { it.interpolationDuration = ANIMATION_TICKS }
    }

    fun createPlayerRow(bp: BotBowsPlayer, yOffset: Float) {
        val nameDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
            text(bp.name)
            val rightAlign = -textWidth(bp.plainName) / 2
            transformation = transformation.apply { translation.set(X + absoluteX + rightAlign - 6*PX, absoluteY + yOffset, 0.01f); }
            billboard = Display.Billboard.VERTICAL
        }

        val headDisplay = Main.WORLD.spawn(loc, ItemDisplay::class.java).apply {
            setItemStack(bp.avatar.headItem)
            transformation = transformation.apply {
                translation.set( absoluteX - 3*PX, absoluteY + yOffset + 8*PX, 0.01f)
                scale.set(0.25f, 0.25f, 0.25f)
                leftRotation.set(AxisAngle4f(Math.toRadians(180.0).toFloat(), 0f, 1f, 0f))
            }
            billboard = Display.Billboard.VERTICAL
        }
        textDisplays.add(nameDisplay)
        headDisplays.add(headDisplay)
    }

    override fun positionX() {
        textDisplays.forEach { it.animate {
            val playerName = PlainTextComponentSerializer.plainText().serialize(it.text())
            translation.x = X + absoluteX - textWidth(playerName) / 2 - 6*PX
        } }
        headDisplays.forEach { it.animate { translation.x = absoluteX - 3*PX } }
    }

    override fun positionY() {
        textDisplays.forEach { it.animate { translation.y = absoluteY } }
        headDisplays.forEach { it.animate { translation.y = absoluteY } }
    }

    fun remove() {
        textDisplays.forEach { it.remove() }
        headDisplays.forEach { it.remove() }
    }
}