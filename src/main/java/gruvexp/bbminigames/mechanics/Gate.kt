package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.botbowsGames.SteamPunkGame
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3i

class Gate(
    private val structureSrc: Location, // where the door parts are located
    private val animationSteps: Int,
    private val size: Vector3i, // where the door is in the map
    private val location: Location,
    private val animationStepTicks: Int,
    private var open: Boolean,
    private val gears: Set<Gear>
) {
    fun toggle() {
        if (open) close()
        else open()
        open = !open
    }

    fun open() {
        object : BukkitRunnable() {
            var step: Int = 0
            override fun run() {
                cloneBlocks(step)
                step++
                if (step == animationSteps) cancel()
            }
        }.runTaskTimer(Main.getPlugin(), 0, animationStepTicks.toLong())
        val delay = (SteamPunkGame.DOOR_TOGGLE_DELAY - ROTATION_ANGLE / gears.first().rotationSpeed).toInt()
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            gears.forEach { it.rotate(ROTATION_ANGLE.toFloat()) }
        }, delay.toLong())
    }

    fun close() {
        object : BukkitRunnable() {
            var step: Int = animationSteps - 1
            override fun run() {
                cloneBlocks(step)
                step--
                if (step < 0) cancel()
            }
        }.runTaskTimer(Main.getPlugin(), 0, animationStepTicks.toLong())
    }

    private fun cloneBlocks(step: Int) {
        val stepOrigin = structureSrc.clone().add((step * (size.x + STRUCTURE_SRC_SPACING)).toDouble(), 0.0, 0.0)
        for (x in 0..<size.x) {
            for (y in 0..<size.y) {
                for (z in 0..<size.z) {
                    val data = Main.WORLD.getBlockData(
                        stepOrigin.blockX + x,
                        stepOrigin.blockY + y,
                        stepOrigin.blockZ + z
                    )
                    location.getWorld().setBlockData(
                        location.blockX + x,
                        location.blockY + y,
                        location.blockZ + z,
                        data
                    )
                }
            }
        }
    }

    companion object {
        private const val STRUCTURE_SRC_SPACING = 1
        private const val ROTATION_ANGLE = 360
    }
}
