package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.util.loadStructure
import gruvexp.bbminigames.util.placeSymmetricalStructure
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.scheduler.BukkitRunnable

class Rotor(id: Int, location: Location, tag: String, speed: Float, teleportDuration: Int) {
    private val displays = mutableSetOf<BlockDisplay>()
    private val rotationStep: Float = speed * listOf(1, -1).random() * ((100..112).random() / 100f)
    private var runnable: BukkitRunnable? = null

    private var jaw = 0f

    init {
        location.getNearbyEntities(2.0, 2.0, 2.0)
            .filterIsInstance<BlockDisplay>()
            .filter { "@{tag}_$id" in it.scoreboardTags }
            .forEach {
                it.setRotation(it.yaw, 0f)
                displays.add(it)
            }
        if (displays.isEmpty()) {
            loadStructure(tag)?.let { structure ->
                val size = structure.size.multiply(0.5)
                placeSymmetricalStructure(
                    structure,
                    location.clone()
                        .add(-size.blockX.toDouble(), -size.blockY.toDouble(), -size.blockZ.toDouble()),
                    location.clone().add(0.5, 0.5, 0.5),
                    StructureRotation.NONE,
                    teleportDuration,
                    "@{tag}_$id",
                    displays
                )
            }
        }
    }

    fun startRotating() {
        runnable = object : BukkitRunnable() {
            override fun run() {
                jaw += rotationStep
                displays.forEach { it.setRotation(jaw, 0f) }
            }
        }.apply { runTaskTimer(Main.plugin, 0, 1) }
    }

    fun stop() {
        runnable!!.cancel()
    }
}
