package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class HealingAbility(bp: BotBowsPlayer, hotbarSlot: Int) : Ability(bp, hotbarSlot, AbilityType.HEALING) {

    var healingProcess: BukkitTask? = null

    override fun use() {
        if (bp.isFullyHealed) {
            bp.avatar.message(Component.text("You are already fully healed", NamedTextColor.YELLOW))
            return
        }
        healingProcess = HealingProcess().runTaskTimer(Main.plugin, 10, 1)
    }

    override fun destroy() {
        super.destroy()
        healingProcess?.cancel()
    }

    override fun reset() {
        super.reset()
        healingProcess?.cancel() //TODO: make cooldown instead just fast and smoothly to go 0 like if you moved
    }

    inner class HealingProcess() : BukkitRunnable() {

        var tick = 0
        val orbs = mutableListOf(Orb(0.0, bp.location))
        var isHealing = true

        override fun run() {
            tick++
            spiral()
            if (isHealing) progressSpiral()

            if (tick == HEAL_TIME) {
                isHealing = false // healing is complete, only tick finish-animations
                registerSuccess()
                bp.heal() //TODO: legg t hjertepartikler
                super@HealingAbility.use() // the countdown will start once the healing is complete
            } else if (tick == HEAL_TIME + EXTRA_ANIMATION_TIME) {
                cancel()
                healingProcess = null
            } else if (tick < 0) {
                super@HealingAbility.use() // or if you cancel the healing by moving
            }
        }

        fun spiral() { // spiral at the edge that loops around (4 orbs)
            val radius = 2
            val bpLoc = bp.location
            for (i in 0..3) {
                val progress = tick % 20 / 20.0
                val θ = (progress + i) * PI / 2
                val x = bpLoc.x + radius * cos(θ)
                val z = bpLoc.z + radius * sin(θ)
                var y = bpLoc.y
                var size = OUTER_ORB_SIZE
                if (!isHealing) {
                    val postProgress = (tick - HEAL_TIME).toDouble() / EXTRA_ANIMATION_TIME // normalized = [0..1]
                    y += postProgress * postProgress * 4 // y will rise quadratic aka accelerating
                    size -= postProgress.toFloat() * OUTER_ORB_SIZE
                }
                bp.location.world.spawnParticle(
                    Particle.DUST,
                    Location(bpLoc.world, x, y, z),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.4,
                    DustOptions(Color.fromRGB(0xff8888), size),
                    true
                )
            }
        }

        fun progressSpiral() {
            val θMax = tick * 2*PI / HEAL_TIME
            orbs.forEach { it.tick(θMax) }

            // number of inner rings (there wont be a ring at the edge bc theres already one there from the other spiral)
            val n = RING_PARTITIONS - 1
            val totalDistance = n*(n + 1)/2 * RADIUS_STEP * θMax

            var i = 0
            while (i < orbs.size) { // going thru each orb to check distance to prev orb and spawn a new one if its too big
                val orb = orbs[i]

                val prevOrb = if (i == 0) orbs.last() else orbs[i - 1]
                var distanceToPrev = orb.totalDistance - prevOrb.totalDistance
                if (distanceToPrev <= 0) distanceToPrev += totalDistance

                if (distanceToPrev > ORB_COVER) { // to big distance to prev orb, make new orb so there wont be a gap
                    val newOrb = orb.createNewOrb(distanceToPrev / 2)
                    orbs.add(i, newOrb)
                } else {
                    val distanceNormalized = distanceToPrev / ORB_COVER
                    // since the distance was just a bit bigger than the orb cover before this orb got created,
                    // it might be very short now, about half of orb cover
                    // therefore the orbs will be closer together and doesnt need to be max size
                    // they will smoothly increase to max size as they reach the orb cover
                    orb.size = distanceNormalized.toFloat()
                }
                i++
            }
        }

        inner class Orb(var θMax: Double, val bpLoc: Location, var size: Float = 1f, ring: Int = 1, θ: Double = 0.0) {
            val id = orbId++

            var ring: Int = -1
                private set(value) {
                    field = value
                    ringRadius = RADIUS_STEP * ring
                    ω = ORB_SPEED/ringRadius
                }

            var ringRadius: Float = RADIUS_STEP * ring
                private set

            var θ: Double = -1.0
                private set(value) {
                    field = min(value, θMax)
                    distance = θ * ringRadius
                    recalculateTotalDistance()
                }

            var ω: Double = ORB_SPEED/ringRadius
                private set

            var distance: Double = 0.0
                private set

            var totalDistance: Double = 0.0
                private set

            init {
                // setting these fields like this to activate the setter logic
                this.ring = ring
                this.θ = θ
            }

            fun recalculateTotalDistance() {
                val n = ring - 1
                val prevDistance = n*(n + 1)/2 * RADIUS_STEP * θMax
                totalDistance = prevDistance + distance
            }

            fun tick(progressΘ: Double) {
                θMax = progressΘ
                θ += ω

                val color = Color.fromRGB(if (θ == θMax) 0xffcc44 else 0xffcccc)
                val size = if (θ == θMax) 1.5f * size else size

                val x = bpLoc.x + ringRadius * cos(θ)
                val z = bpLoc.z + ringRadius * sin(θ)
                val particleLoc = Location(bpLoc.world, x, bpLoc.y, z)
                bpLoc.world.spawnParticle(
                    Particle.DUST,
                    particleLoc,
                    2,
                    0.05,
                    0.0,
                    0.05,
                    0.4,
                    DustOptions(color, size),
                    true
                )
                if (θ == θMax) {
                    ring++
                    if (ring >= RING_PARTITIONS) ring = 1
                    θ = 0.0
                }
            }

            fun createNewOrb(Δdist: Double): Orb { // creates the orb halfway between the prev orb
                var distLeft = Δdist
                var currentRing = ring
                var currentDist = distance

                while (distLeft >= 0) {
                    if (currentDist > distLeft) {
                        currentDist -= distLeft
                        val currentθ = currentDist / (currentRing * RADIUS_STEP)
                        return Orb(θMax, bpLoc, 0.5f, currentRing, currentθ)
                    }
                    distLeft -= currentDist
                    currentRing --
                    if (currentRing == 0) currentRing = RING_PARTITIONS - 1
                    currentDist = ringDistance(θMax, currentRing)
                }
                error("This line will never be reached")
            }
        }
    }

    companion object {
        const val HEAL_TIME = 20 * 20
        private const val EXTRA_ANIMATION_TIME = 4 * 20

        // visual effects
        const val OUTER_ORB_SIZE = 2f
        // inner orbs
        const val RING_PARTITIONS = 10
        const val RING_RADIUS = 2
        const val ORB_SPEED = 0.2

        // how long distance the orb will cover, since the particles will go out after some time
        // and when they do, it will be seen as the end of the cover, and a new particle will be spawned in
        const val ORB_COVER = 3.0
        const val RADIUS_STEP = RING_RADIUS.toFloat() / RING_PARTITIONS

        var orbId = 0

        fun ringDistance(currentΘ: Double, ringSector: Int): Double {
            val ringRadius = ringSector.toFloat() / RING_PARTITIONS * RING_RADIUS
            return currentΘ * ringRadius
        }
    }

}