package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.abs

class Gear @JvmOverloads constructor(
    id: Int,
    location: Location,
    rotation: StructureRotation,
    tag: String,
    private val rotationStep: Float,
    teleportDuration: Int = 1
) {
    private val displays = mutableSetOf<BlockDisplay>()
    private val jaw: Float = if (rotation == StructureRotation.CLOCKWISE_90 || rotation == StructureRotation.COUNTERCLOCKWISE_90) 90f else 0f
    private var runnable: BukkitRunnable? = null

    private var pitch = 0f

    init {
        location.getNearbyEntities(10.0, 10.0, 10.0)
            .filterIsInstance<BlockDisplay>()
            .filter { "${tag}_$id" in it.scoreboardTags }
            .forEach {
                it.setRotation(it.yaw, 0f)
                displays.add(it)
            }
        if (displays.isEmpty()) {
            BotBows.loadStructure(tag)?.let { structure ->
                val size = structure.size.multiply(0.5)
                BotBows.placeSymmetricalStructure(
                    structure,
                    location.clone()
                        .add(-size.blockX.toDouble(), -size.blockY.toDouble(), -size.blockZ.toDouble()),
                    location.clone().add(0.0, 0.5, 0.5),
                    rotation,
                    teleportDuration,
                    "${tag}_$id",
                    displays
                )
            }
        }
    }

    private fun rotate() {
        pitch += rotationStep
        if (pitch > 91) {
            rotateTo(-90f)
        } else if (pitch < -91) {
            rotateTo(90f)
        }
        displays.forEach { it.setRotation(jaw, pitch) }
    }

    fun rotate(degrees: Float) {
        val stepDegrees = abs(rotationStep)

        runnable = object : BukkitRunnable() {
            var degreesLeft: Float = degrees

            override fun run() {
                if (degreesLeft <= 0) cancel()

                rotate()
                degreesLeft -= stepDegrees
            }
        }.apply { runTaskTimer(Main.getPlugin(), 0, 1) }
    }

    private fun rotateTo(newPitch: Float) {
        displays.forEach { it.setRotation(jaw, newPitch) }
        pitch = newPitch
    }

    fun stop() {
        runnable!!.cancel()
    }

    val rotationSpeed: Float
        get() = abs(rotationStep)
}
