package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.util.editData
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Directional
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3i

class SpaceStationDoor(private val location: Location, private val axis: Axis) {
    private val shulkerBoxes = if (axis == Axis.X) {
        setOf(
            location.clone().add(1.0, 3.0, 0.0),
            location.clone().add(-1.0, 3.0, 0.0),
            location.clone().add(1.0, 3.0, 4.0),
            location.clone().add(-1.0, 3.0, 4.0),
        )
    } else {
        setOf(
            location.clone().add(0.0, 3.0, 1.0),
            location.clone().add(0.0, 3.0, -1.0),
            location.clone().add(4.0, 3.0, 1.0),
            location.clone().add(4.0, 3.0, -1.0),
        )
    }.map { it.block }.toSet()

    var isOpen: Boolean = false
        private set

    fun toggle() {
        if (isOpen) close()
        else open()
    }

    fun open() {
        isOpen = true
        setShulkerBoxes()
        setOutline()
        object : BukkitRunnable() {
            var step = ANIMATION_STEPS - 1
            override fun run() {
                cloneBlocks(step)
                step--
                if (step < 0) cancel()
            }
        }.runTaskTimer(Main.getPlugin(), 0, ANIMATION_STEP_TICKS.toLong())
    }

    fun close() {
        isOpen = false
        setShulkerBoxes()
        setOutline()
        object : BukkitRunnable() {
            var step: Int = 0
            override fun run() {
                cloneBlocks(step)
                step++
                if (step == ANIMATION_STEPS) cancel()
            }
        }.runTaskTimer(Main.getPlugin(), 0, ANIMATION_STEP_TICKS.toLong())
    }

    private fun cloneBlocks(step: Int) {
        val offset = if (isOpen) 5.0 else 0.0
        val stepOrigin: Location = if (axis == Axis.X) STRUC_SRC_X.clone()
            .add(step.toDouble(), 0.0, offset) else STRUC_SRC_Z.clone()
            .add(offset, 0.0, step.toDouble())
        val size: Vector3i = if (axis == Axis.X) SIZE_X else SIZE_Z
        for (x in 0..<size.x) {
            for (y in 0..<size.y) {
                for (z in 0..<size.z) {
                    val data = Main.WORLD_END.getBlockData(
                        stepOrigin.blockX + x,
                        stepOrigin.blockY + y,
                        stepOrigin.blockZ + z
                    )
                    location.world.setBlockData(
                        location.blockX + x,
                        location.blockY + y,
                        location.blockZ + z,
                        data
                    )
                }
            }
        }
    }

    fun setShulkerBoxes() {
        val material = if (isOpen) Material.LIME_SHULKER_BOX else Material.RED_SHULKER_BOX
        shulkerBoxes.forEach { block ->
            val oldFacing = (block.blockData as Directional).facing
            block.type = material
            block.editData<Directional> { facing = oldFacing }
        }
    }

    fun setOutline() {
        val size: Vector3i = if (axis == Axis.X) SIZE_X else SIZE_Z
        for (x in -2..<size.x + 2) {
            for (y in -1..<size.y + 1) {
                for (z in -2..<size.z + 2) {
                    val block = location.world.getBlockAt(location.blockX + x, location.blockY + y, location.blockZ + z)
                    if (isOpen && block.type == Material.RED_CONCRETE) {
                        block.type = Material.LIME_CONCRETE
                    } else if (!isOpen && block.type == Material.LIME_CONCRETE) {
                        block.type = Material.RED_CONCRETE
                    }
                }
            }
        }
    }

    companion object {
        private const val ANIMATION_STEP_TICKS = 2
        private val STRUC_SRC_X = Location(Main.WORLD_END, 139.0, 2.0, 195.0)
        private val STRUC_SRC_Z = Location(Main.WORLD_END, 129.0, 2.0, 205.0)
        private val SIZE_X = Vector3i(1, 7, 5)
        private val SIZE_Z = Vector3i(5, 7, 1)
        private const val ANIMATION_STEPS = 4
    }
}
