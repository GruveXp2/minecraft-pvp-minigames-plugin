package gruvexp.bbminigames.util

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.scheduler.BukkitRunnable

// inline = not its own function but just copypasted into code at compile time
// reified: if its off the compiler only uses the T to check and give errors if u use it wrong. if its on, the type actually stays so you can use it further
inline fun <reified T : BlockData> Block.editData(action: T.() -> Unit) {
    blockData = (blockData as T).apply(action)
}

fun TextColor.lighten(factor: Float): TextColor {
    var r = red()
    var g = green()
    var b = blue()

    r += ((255 - red()) * factor).toInt()
    g += ((255 - green()) * factor).toInt()
    b += ((255 - blue()) * factor).toInt()

    return TextColor.color(r, g, b)
}

fun World.setTimeSmooth(start: Long, end: Long, seconds: Int) {
    val ticks = seconds * 20
    val step = (end - start) / ticks
    object : BukkitRunnable() {
        var count: Long = 0
        override fun run() {
            if (count >= ticks) cancel()
            else time = start + (count++ * step)
        }
    }.runTaskTimer(Main.plugin, 0, 1)
}

fun Location.getChunksAround(blockRadius: Int): MutableSet<Chunk> {
    val chunks = mutableSetOf<Chunk>()

    val minX = (blockX - blockRadius) shr 4
    val maxX = (blockX + blockRadius) shr 4
    val minZ = (blockZ - blockRadius) shr 4
    val maxZ = (blockZ + blockRadius) shr 4

    for (chunkX in minX..maxX) {
        for (chunkZ in minZ..maxZ) {
            chunks.add(world.getChunkAt(chunkX, chunkZ))
        }
    }
    return chunks
}


fun Location.getOrthogonalLocations(axis: Axis): MutableSet<Location> {
    val orthogonals = mutableSetOf<BlockFace>()
    for (face in BlockFace.entries) {
        val faceAxis = face.getAxis() ?: continue
        if (faceAxis != axis) {
            orthogonals.add(face)
        }
    }
    val locations = mutableSetOf<Location>()
    orthogonals.forEach { face -> locations.add(clone().add(face.direction)) }
    return locations
}

fun BlockFace.getAxis(): Axis? {
    if (modX != 0 && modY == 0 && modZ == 0) return Axis.X
    if (modY != 0 && modX == 0 && modZ == 0) return Axis.Y
    if (modZ != 0 && modX == 0 && modY == 0) return Axis.Z
    return null
}