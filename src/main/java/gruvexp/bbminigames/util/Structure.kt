package gruvexp.bbminigames.util

import gruvexp.bbminigames.twtClassic.BotBows.debugMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.structure.Structure
import java.util.Random
import kotlin.collections.forEach

fun placeSymmetricalStructure(
    structure: Structure,
    location: Location,
    centerLocation: Location,
    rotation: StructureRotation,
    teleportDuration: Int,
    tag: String,
    displays: MutableSet<BlockDisplay>
) {
    val bottomLocation = location.clone().add(0.0, -50.0, 0.0)
    structure.place(bottomLocation, false, StructureRotation.NONE, Mirror.NONE, 0, 1f, Random(0))
    val start = bottomLocation.toVector().toBlockVector()
    val size = structure.size
    val world = location.world

    val bottomCenter = centerLocation.clone().add(0.0, -50.0, 0.0)
    for (relX in 0..<size.blockX) {
        for (relY in 0..<size.blockY) {
            for (relZ in 0..<size.blockZ) {
                val x = start.blockX + relX
                val y = start.blockY + relY
                val z = start.blockZ + relZ
                val block = world.getBlockAt(x, y, z)
                if (block.type != Material.AIR) {
                    // turn the block into a block display
                    val display = world.spawn(
                        Location(world, x.toDouble(), y.toDouble(), z.toDouble()),
                        BlockDisplay::class.java
                    )
                    display.block = block.blockData
                    display.addScoreboardTag(tag)
                    displays.add(display)
                    block.type = Material.AIR

                    // tp the block to the center, but make it display where it was
                    val Δpos = display.location.subtract(bottomCenter).toVector().toVector3f()
                    display.teleport(centerLocation)
                    val transformation = display.transformation
                    transformation.translation.set(Δpos)
                    display.transformation = transformation
                    display.teleportDuration = teleportDuration
                }
            }
        }
    }

    val yaw = when (rotation) {
        StructureRotation.NONE -> 0
        StructureRotation.CLOCKWISE_90 -> 90
        StructureRotation.CLOCKWISE_180 -> 180
        StructureRotation.COUNTERCLOCKWISE_90 -> -90
    }
    displays.forEach {
        val loc = it.location
        loc.yaw = yaw.toFloat()
        it.teleport(loc)
    }
}

fun loadStructure(name: String): Structure? {
    val structure = Bukkit.getStructureManager().loadStructure(NamespacedKey("botbows", name))
    if (structure == null) {
        debugMessage("ERROR! Structure \"botbows:$name\" failed to load")
        return null
    }
    return structure
}