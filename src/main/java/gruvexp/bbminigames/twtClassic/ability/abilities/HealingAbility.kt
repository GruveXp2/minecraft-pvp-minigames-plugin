package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
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
        super.use()
        BotBows.debugMessage("Now using healing")
        //registerSuccess()
        healingProcess = HealingProcess().runTaskTimer(Main.plugin, 10, 1)
    }

    override fun destroy() {
        super.destroy()
        healingProcess?.cancel()
    }

    override fun reset() {
        super.reset()
        healingProcess?.cancel() // maybe instead just make progress smoothly go to 0
    }

    inner class HealingProcess() : BukkitRunnable() {

        var tick = 0
        val orbs = mutableListOf(Orb(0.0, bp.location)) // ska bare være ei liste og så gjøres sjekk og evt orb creating hver/annehver tiuck

        override fun run() {
            tick++
            spiral()
            progressSpiral()

            if (tick == HEAL_TIME) {
                cancel()
                healingProcess = null
            }
        }

        fun spiral() {
            val radius = 2
            val bpLoc = bp.location
            for (i in 0..3) {
                val progress = tick % 20 / 20.0
                val θ = (progress + i) * PI / 2
                val x = bpLoc.x + radius * cos(θ)
                val z = bpLoc.z + radius * sin(θ)
                bp.location.world.spawnParticle(
                    Particle.DUST,
                    Location(bpLoc.world, x, bpLoc.y, z),
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.4,
                    DustOptions(Color.fromRGB(0xff8888), 2f),
                    true
                )
            }
        }

        fun progressSpiral() {
            BotBows.debugMessage("TICK =====================================================================")
            val θMax = tick * 2*PI / HEAL_TIME
            orbs.forEach { it.tick(θMax) }

            var i = 0

            val n = RING_PARTITIONS - 1 // number of inner rings
            val totalDistance = n*(n + 1)/2 * RADIUS_STEP * θMax
            BotBows.debugMessage("")
            BotBows.debugMessage("finished ticking the orbs. totalDistance = ${"%.3f".format(totalDistance)}")

            while (i < orbs.size) {
                val orb = orbs[i]

                val prevOrb = if (i == 0) orbs.last() else orbs[i - 1]
                var distanceToPrev = orb.totalDistance - prevOrb.totalDistance
                if (distanceToPrev <= 0) distanceToPrev += totalDistance
                BotBows.debugMessage("Orb${orb.id} ∂istToPrev" +
                        " = Orb${orb.id}.totDist(${"%.3f".format(orb.totalDistance)})" +
                        " - Orb${prevOrb.id}.totDist(${"%.3f".format(prevOrb.totalDistance)})" +
                        " = ${"%.3f".format(orb.totalDistance - prevOrb.totalDistance)}" +
                        if (orb.totalDistance - prevOrb.totalDistance <= 0) "-> ${"%.3f".format(distanceToPrev)}" else ""
                )

                if (distanceToPrev > ORB_COVER) {
                    BotBows.debugMessage("Adding new orb. global totalDistance: ${"%.3f".format(totalDistance)}, currentOrb.distanceToPrev: ${"%.3f".format(distanceToPrev)}")
                    val newOrb = orb.createNewOrb(distanceToPrev / 2)
                    orbs.add(i, newOrb)
                    var newOrbTotDist = orb.totalDistance - distanceToPrev / 2
                    if (newOrbTotDist < 0) newOrbTotDist += totalDistance
                    BotBows.debugMessage("new orb totalDistance should be: ${"%.3f".format(newOrbTotDist)}, is actually ${"%.3f".format(newOrb.totalDistance)}")
                    Bukkit.broadcast(Component.text("The orb is added!", NamedTextColor.YELLOW))
                } else {
                    val distanceNormalized = distanceToPrev / ORB_COVER
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
                    ringRadius = RADIUS_STEP * ring // bruh, i used the old value!!!
                    ω = ORB_SPEED/ringRadius
                }

            var ringRadius: Float = RADIUS_STEP * ring
                private set

            var θ: Double = -1.0
                private set(value) {
                    field = min(value, θMax)
                    distance = θ * ringRadius
                    BotBows.debugMessage(" - Orb$id::θ.-> distance = θ(${"%.3f".format(θ)}) * ringRadius($ringRadius) = ${"%.3f".format(θ * ringRadius)}")

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
                BotBows.debugMessage(" - (re)calculating Orb$id::totalDistance")
                val n = ring - 1
                val prevDistance = n*(n + 1)/2 * RADIUS_STEP * θMax // bruh why multiplyign with degrees 2 times??!?!?!?!?
                BotBows.debugMessage(" - - Orb$id::prevDistance = ringSizes(${n*(n + 1)/2} * $RADIUS_STEP) * θMax(${"%.3f".format(θMax)}) = $prevDistance")
                totalDistance = prevDistance + distance
                BotBows.debugMessage(" - - Orb$id::totalDistance = prev(${"%.3f".format(prevDistance)} + dist(${"%.3f".format(distance)}) = ${"%.3f".format(totalDistance)} (currentRing=$ring)")
            }

            fun tick(progressΘ: Double) {
                BotBows.debugMessage("Now ticking    Orb$id -> " +
                        "ring: $ring, " +
                        "θ: ${"%.3f".format(θ)}, " +
                        "totalDist: ${"%.3f".format(totalDistance)}, " +
                        "θMax: ${"%.3f".format(θMax)}, " +
                        "ω: ${"%.3f".format(ω)}")
                θMax = progressΘ
                //BotBows.debugMessage("progressΘ: $progressΘ, θ: $θ")
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
                BotBows.debugMessage("Finish ticking Orb$id -> " +
                        "ring: $ring, " +
                        "θ: ${"%.3f".format(θ)}, " +
                        "totalDist: ${"%.3f".format(totalDistance)}")
            }

            fun createNewOrb(Δdist: Double): Orb { // vi må gå baggover istedet
                var distLeft = Δdist
                var currentRing = ring

                var test = 0
                var currentDist = distance

                while (distLeft >= 0) {
                    test++
                    if (currentDist > distLeft) {
                        currentDist -= distLeft
                        val currentθ = currentDist / (currentRing * RADIUS_STEP)
                        return Orb(θMax, bpLoc, 0.5f, currentRing, currentθ)
                    }
                    distLeft -= currentDist
                    currentRing --
                    if (currentRing == 0) currentRing = RING_PARTITIONS - 1
                    currentDist = ringDistance(θMax, currentRing)
                    if (test == 12) {
                        BotBows.debugMessage("seomthing wrong in the while loop, over 12 loops")
                        break
                    }
                }

                Bukkit.broadcast(Component.text("\\/ \\/ \\/ \\/ \\/", NamedTextColor.RED, TextDecoration.BOLD))

                var distLeft2 = Δdist
                var currentRing2 = ring
                BotBows.debugMessage("Start: ")

                var test2 = 0
                var currentDist2 = distance

                while (distLeft2 >= 0) {
                    test2++
                    BotBows.debugMessage("loop $test2: distLeft: ${"%.3f".format(distLeft2)}, currentRing: $currentRing2")
                    if (currentDist2 > distLeft2) {
                        currentDist2 -= distLeft2
                        return Orb(θMax, bpLoc, 0.5f, currentRing2, currentDist2) // bruh whys it using variables from up there?!?!?
                    }
                    distLeft2 -= currentDist2
                    currentRing2 --
                    if (currentRing2 == 0) currentRing2 = RING_PARTITIONS - 1
                    currentDist2 = ringDistance(θMax, currentRing2)
                    if (test2 == 20) BotBows.debugMessage("as you can see, its bugging")
                }
                cancel()
                error("This line will never be reached")
            }
        }
    }

    companion object {
        const val HEAL_TIME = 20 * 20
        mu
        // Visual effects
        const val RING_PARTITIONS = 10
        const val RING_RADIUS = 2
        const val ORB_SPEED = 0.15

        // how long distance the orb will cover, since the particles will go out after some time
        // and when they do, it will be seen as the end of the cover, and a new particle will be spawned in
        const val ORB_COVER = 2.0
        const val RADIUS_STEP = RING_RADIUS.toFloat() / RING_PARTITIONS

        var orbId = 0

        fun ringDistance(currentΘ: Double, ringSector: Int): Double {
            val ringRadius = ringSector.toFloat() / RING_PARTITIONS * RING_RADIUS
            return currentΘ * ringRadius
        }
    }

}