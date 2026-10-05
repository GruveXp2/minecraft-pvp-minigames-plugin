package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.util.getChunksAround
import gruvexp.bbminigames.util.loadStructure
import gruvexp.bbminigames.util.placeSymmetricalStructure
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Vector
import kotlin.math.atan2
import kotlin.math.sqrt

class Impeller(
    id: Int,
    structureName: String,
    private val centerLocation: Location,
    private val rotationSpeed: Float
) {
    private val displays = mutableSetOf<BlockDisplay>()

    private var jaw = 0f
    private val players = mutableSetOf<BotBowsPlayer>()

    init {
        centerLocation.getNearbyEntities(2.0, 2.0, 2.0)
            .filterIsInstance<BlockDisplay>()
            .filter { "${structureName}_$id" in it.scoreboardTags }
            .forEach {
                it.setRotation(0f, 0f)
                displays.add(it)
            }
        if (displays.isEmpty()) {
            loadStructure(structureName)?.let { structure ->
                val originLoc = centerLocation.clone().add(-2.0, 0.0, -2.0)
                placeSymmetricalStructure(
                    structure,
                    originLoc,
                    centerLocation.clone().add(0.5, 0.5, 0.5),
                    StructureRotation.NONE,
                    5,
                    "${structureName}_$id",
                    displays
                )
            }
        }
    }

    val tickedChunks: Set<Chunk>
        // the chunks close to this impeller, only check for players if players are in these chunks
        get() = centerLocation.getChunksAround(3)

    fun checkProximity(bp: BotBowsPlayer) {
        if (bp in players) return

        if (bp.location.distanceSquared(centerLocation) < 9) { // radius 3
            players.add(bp)
        }
    }

    fun tick() {
        jaw += rotationSpeed
        if (jaw < 0) {
            jaw += 360f
        } else if (jaw > 360) {
            jaw %= 360
        }
        displays.forEach { it.setRotation(jaw, 0f) }

        players.forEach { bp ->
            val entity = bp.avatar.entity
            val relDir = centerLocation.clone().subtract(entity.location).toVector() // retningsvektoren
            relDir.y = 0.0
            val x = relDir.x
            val z = relDir.z
            // polar form
            val r = sqrt(x * x + z * z)
            val θ = atan2(z, x)

            var pJaw = Math.toDegrees(θ) - 90
            pJaw -= jaw // relative to the rotation of the impeller
            // make into interval [0,90)
            pJaw = (pJaw + 720) % 90
            if (if (rotationSpeed > 0) pJaw < 20 else pJaw > 70) { // they hit the impeller and will get pushed
                val divide = 5 + (if (rotationSpeed > 0) pJaw else 90 - pJaw) * 7
                val push = relDir.crossProduct(Vector(0f, -rotationSpeed, 0f))
                    .multiply(r / divide) // retning x up||down = vel from hitting the bl8d

                val v = entity.velocity
                val pushDir = push.clone().normalize()
                val vAlongPushDir = v.dot(pushDir)
                v.subtract(pushDir.multiply(vAlongPushDir))
                    .add(push) // the part of v in the direction pushed gets completly replaced with the push value
                entity.velocity = v
            } else if (if (rotationSpeed > 0) pJaw > 70 else pJaw < 20) { // they hit the impeller and will get pushed
                val dv = relDir.crossProduct(Vector(0f, rotationSpeed, 0f))
                    .multiply(r / 25) // retning x up||down = vel from hitting the bl8d
                val v = entity.velocity
                v.add(dv)
                entity.velocity = v
            }
            if (r > 3.1) {
                players.remove(bp)
            }
        }
    }
}
