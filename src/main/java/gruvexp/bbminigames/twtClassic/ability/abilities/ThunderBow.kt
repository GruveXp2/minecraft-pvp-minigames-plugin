package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityContext.Launch
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnLaunch
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnProjectileHit
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Arrow
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.random.Random

class ThunderBow(bp: BotBowsPlayer, hotBarSlot: Int)
    : Ability(bp, hotBarSlot, AbilityType.THUNDER_BOW), OnLaunch, OnProjectileHit {
    var isActive: Boolean = false
        private set

    override fun use() {
        super.use()
        isActive = true
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { isActive = false }, 20L * DURATION)
    }

    override fun onLaunch(ctx: Launch) {
        val arrow = ctx.projectile as Arrow
        arrow.color = Color.AQUA
        val arrowTrail = ThunderArrowTrailGenerator(arrow, bp.team.dyeColor.color).runTaskTimer(Main.getPlugin(), 1L, 1L)
        activeArrows[arrow] = arrowTrail
        arrow.setMetadata("botbows_ability", FixedMetadataValue(Main.getPlugin(), this))
        BotBows.debugMessage("Spawning a thunder arrow", TestCommand.test2)
    }

    override fun onHit(e: ProjectileHitEvent) {
        val arrow = e.getEntity() as Arrow
        e.hitBlock?.let {
            val hitLoc = it.location
            handleArrowHitBlock(hitLoc)
            activeArrows[arrow]!!.cancel()
            activeArrows.remove(arrow)
            return@onHit
        }
        val defender = BotBows.getBotBowsPlayer(e.hitEntity!!.uniqueId)

        if (defender != null) {
            handleArrowHitPlayer(bp, defender)
        }

        activeArrows.remove(arrow)?.cancel()
        arrow.remove()
    }

    class ThunderArrowTrailGenerator(private val arrow: Arrow, private val color: Color) : BukkitRunnable() {
        override fun run() {
            arrow.world.spawnParticle(
                Particle.DUST,
                arrow.location,
                5,
                0.1,
                0.1,
                0.1,
                0.5,
                DustOptions(Color.WHITE, 1f),
                true
            )
            arrow.world.spawnParticle(
                Particle.DUST,
                arrow.location,
                5,
                0.1,
                0.1,
                0.1,
                0.3,
                DustOptions(color, 0.5f),
                true
            )
            arrow.velocity.add(Vector(0.0, 0.03, 0.0))

            val spark: Vector = getRandomPerpendicular(arrow.velocity).multiply(Random.nextDouble(1.0, 3.0))
            val sparkLocation = arrow.location.add(spark)

            if (BotBows.RANDOM.nextInt(3) == 0 && sparkLocation.block.type != Material.AIR) {
                createElectricArc(arrow.location, sparkLocation, Color.AQUA, 2.0, false)
            }
        }
    }

    companion object {
        val THUNDER_BOW = Menu.makeItem( //TODO: custom skin som erstatter vanlig crossbow vises
            Material.CROSSBOW,
            "thunder_bow",
            Component.text("ThunderBow"),
            Component.text("Shoots electric arrows")
        )
        const val CHAIN_RADIUS: Double = 8.0
        const val DURATION: Int = 10 // seconds

        var activeArrows = mutableMapOf<Arrow, BukkitTask>()

        fun handleArrowHitPlayer(attacker: BotBowsPlayer, defender: BotBowsPlayer) {
            if (defender.team == attacker.team) return

            defender.damage(DamageContext.Player(DamageType.Player.THUNDER_BOW, attacker))

            val handledPlayers = mutableSetOf(defender)
            handleChain(attacker, defender, handledPlayers)
        }

        private fun handleChain(
            attacker: BotBowsPlayer,
            defender: BotBowsPlayer,
            handledPlayers: MutableSet<BotBowsPlayer>
        ) {
            val nearbyPlayers = defender.getNearbyPlayers(CHAIN_RADIUS)
                .filter { it.team != attacker.team && it !in handledPlayers }
            if (nearbyPlayers.isEmpty()) return

            val attackerTeamColor = attacker.team.dyeColor.color
            val world = attacker.location.getWorld()
            val ability = attacker.getAbility(AbilityType.THUNDER_BOW)
            for (nearbyPlayer in nearbyPlayers) {
                val nearbyPlayerLoc = nearbyPlayer.location.add(0.0, 1.0, 0.0) // the arc will hit the middle of the player
                world.strikeLightningEffect(nearbyPlayerLoc)
                createElectricArc(defender.location.add(0.0, 1.0, 0.0), nearbyPlayerLoc, attackerTeamColor, 1.0, true)
                nearbyPlayer.damage(DamageContext.Player(DamageType.Player.THUNDER_BOW_CHAIN, attacker))
                handledPlayers.add(nearbyPlayer)
                ability.registerSuccess()
            }
            for (nearbyPlayer in nearbyPlayers) {
                handleChain(attacker, nearbyPlayer, handledPlayers)
            }
        }

        fun handleArrowHitBlock(hitLoc: Location) {
            repeat(10) {
                val x = (-5..5).random()
                val y = (-5..5).random()
                val z = (-5..5).random()
                val randomVec = Vector(x, y, z)
                val arcLoc = hitLoc.clone().add(randomVec)
                if (arcLoc.block.type != Material.AIR) {
                    createElectricArc(hitLoc, arcLoc, Color.AQUA, 2.0, false)
                }
            }
        }

        fun createElectricArc(
            loc1: Location,
            loc2: Location,
            color: Color,
            frequencyMultiplier: Double,
            strong: Boolean
        ) {
            val length = loc1.distance(loc2)
            val diff = Vector(
                loc2.x - loc1.x,
                loc2.y - loc1.y,
                loc2.z - loc1.z
            )
            val steps = (length * frequencyMultiplier).toInt()
            val locations = mutableListOf<Vector>()
            val start = loc1.toVector()
            val end = loc2.toVector()
            locations.add(start)
            for (i in 1..<steps) {
                val t = i.toDouble() / steps // Interpolation factor (0 to 1)
                val x = start.x + t * (end.x - start.x)
                val y = start.y + t * (end.y - start.y)
                val z = start.z + t * (end.z - start.z)
                val vecI = Vector(x, y, z).add(getRandomPerpendicular(diff).multiply(Random.nextDouble()))
                locations.add(vecI)
            }
            locations.add(end)
            val coloredOffset = if (strong) 0.5f else 0.1f
            val coloredAmount = if (strong) 5 else 1
            val whiteDust = DustOptions(Color.WHITE, if (strong) 1.5f else 0.5f)
            val coloredDust = DustOptions(color, if (strong) 1f else 0.8f)
            for (i in 0..locations.size - 2) {
                val rayDiff = locations[i].multiply(-1).add(locations[i + 1]).multiply(0.1)
                for (j in 0..9) {
                    loc1.add(rayDiff)
                    if (j % 2 == 0) {
                        loc1.world.spawnParticle(
                            Particle.DUST,
                            loc1,
                            coloredAmount,
                            coloredOffset.toDouble(),
                            coloredOffset.toDouble(),
                            coloredOffset.toDouble(),
                            10.0,
                            coloredDust
                        )
                    }
                    loc1.world.spawnParticle(
                        Particle.DUST,
                        loc1,
                        5,
                        0.05,
                        0.05,
                        0.05,
                        0.1,
                        whiteDust
                    )
                }
            }
        }

        private fun getRandomPerpendicular(diff: Vector): Vector {
            var reference = Vector(0, 1, 0)

            if (abs(diff.y) > 0.99) { // If it's too close to the y-axis, switch reference
                reference = Vector(1, 0, 0)
            }

            val perpendicular = diff.clone().crossProduct(reference).normalize()
            val angle = Math.random() * 2 * Math.PI

            return perpendicular.rotateAroundAxis(diff, angle)
        }
    }
}
