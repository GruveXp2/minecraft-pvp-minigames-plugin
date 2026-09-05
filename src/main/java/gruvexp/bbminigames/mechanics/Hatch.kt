package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min

class Hatch(id: Int, location: Location, rotation: StructureRotation, structureName: String) {
    private var open = false
    private val displays = mutableSetOf<BlockDisplay>()
    private val openHitbox = mutableSetOf<Block>()
    private val closedHitbox = mutableSetOf<Block>()

    init {
        BotBows.loadStructure(structureName)?.let { structure ->
            val offset = structure.size.add(Vector(-1, -1, -1))
            val openOffset = Vector(offset.blockX, offset.blockZ, offset.blockY)
            val closedTarget = rotateVector(offset, rotation)
            val originLoc = location.clone().add(-1.0, 0.0, 0.0)
            val origin = location.toVector().add(rotateVector(Vector(-1, 0, 0), rotation))
            val openTarget = rotateVector(openOffset, rotation)

            val closedBounds = getBounds(origin, closedTarget)
            val openBounds = getBounds(origin, openTarget)

            val hatchArea = Location(Main.WORLD, origin.x, origin.y, origin.z)
            hatchArea.getNearbyEntities(10.0, 10.0, 10.0)
                .filterIsInstance<BlockDisplay>()
                .filter { "${structureName}_$id" in it.scoreboardTags }
                .forEach {
                    it.setRotation(it.yaw, 0f)
                    displays.add(it)
                }

            if (displays.isEmpty()) {
                BotBows.placeSymmetricalStructure(
                    structure,
                    originLoc,
                    location.clone().add(0.5, 0.5, 0.5),
                    rotation,
                    2,
                    "${structureName}_$id",
                    displays
                )
            }

            run {
                var x = closedBounds[0].blockX
                while (x <= closedBounds[1].x) {
                    var y = closedBounds[0].blockY
                    while (y <= closedBounds[1].y) {
                        var z = closedBounds[0].blockZ
                        while (z <= closedBounds[1].z) {
                            val block = Main.WORLD.getBlockAt(x, y, z)
                            block.type = Material.BARRIER
                            closedHitbox.add(block)
                            z++
                        }
                        y++
                    }
                    x++
                }
            }

            var x = openBounds[0].blockX
            while (x <= openBounds[1].x) {
                var y = openBounds[0].blockY
                while (y <= openBounds[1].y) {
                    var z = openBounds[0].blockZ
                    while (z <= openBounds[1].z) {
                        val block = Main.WORLD.getBlockAt(x, y, z)
                        block.type = Material.AIR
                        openHitbox.add(block)
                        z++
                    }
                    y++
                }
                x++
            }
        }
    }

    private fun getBounds(origin: Vector, size: Vector): Array<Vector> {
        val end = origin.clone().add(size)

        val min = Vector(
            min(origin.x, end.x),
            min(origin.y, end.y),
            min(origin.z, end.z)
        )

        val max = Vector(
            max(origin.x, end.x),
            max(origin.y, end.y),
            max(origin.z, end.z)
        )

        return arrayOf(min, max)
    }

    private fun rotateVector(vec: Vector, rotation: StructureRotation): Vector {
        val x = vec.x
        val z = vec.z

        when (rotation) {
            StructureRotation.CLOCKWISE_90 -> {
                vec.x = -z
                vec.z = x
            }

            StructureRotation.COUNTERCLOCKWISE_90 -> {
                vec.x = z
                vec.z = -x
            }

            StructureRotation.CLOCKWISE_180 -> {
                vec.x = -x
                vec.z = -z
            }

            StructureRotation.NONE -> {
                // Nothing
            }
        }
        return vec
    }

    fun toggle() {
        if (open) close()
        else open()
        open = !open
    }

    fun open() {
        // launch up players that stand on the hatch when it opens
        val players = closedHitbox.first().location.getNearbyEntities(4.0, 2.0, 3.0)
            .filterIsInstance<Player>()
            .filter { it.location.block.getRelative(BlockFace.DOWN).type == Material.BARRIER }
            .toSet()

        closedHitbox.forEach { block -> block.type = Material.AIR }
        players.forEach {
            if (it.location.block.getRelative(BlockFace.DOWN).type == Material.AIR) {
                it.velocity = it.velocity.apply { add(Vector(0, 1, 0)) }
            }
        }
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { openHitbox.forEach { block -> block.type = Material.BARRIER } },
            TOTAL_STEPS / 2L
        )
        // rotate them upwards
        object : BukkitRunnable() {
            val jaw = displays.first().yaw
            var pitch = 0f
            override fun run() {
                pitch -= 90f / TOTAL_STEPS
                displays.forEach { it.setRotation(jaw, pitch) }
                if (pitch <= -90) {
                    cancel()
                    open = true
                }
            }
        }.apply { runTaskTimer(Main.getPlugin(), 0, 1) }
    }

    fun close() {
        openHitbox.forEach { block -> block.type = Material.AIR }
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { closedHitbox.forEach { block -> block.type = Material.BARRIER } },
            TOTAL_STEPS / 2L
        )
        object : BukkitRunnable() {
            val jaw = displays.first().yaw
            var pitch = -90f
            override fun run() {
                pitch += 90f / TOTAL_STEPS
                displays.forEach { it.setRotation(jaw, pitch) }
                if (pitch >= 0) {
                    cancel()
                    open = false
                }
            }
        }.apply { runTaskTimer(Main.getPlugin(), 0, 1) }
    }

    companion object {
        private const val TOTAL_STEPS = 20
    }
}
