package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.Util
import gruvexp.bbminigames.api.ability.AbilityContext.BlockPlace
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnBlockPlace
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Wall
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.abs

class LaserTrap(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.LASER_TRAP), OnBlockPlace {
    private var emitter: LaserEmitter? = null

    override fun onPlace(ctx: BlockPlace) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            removeLaser()
            emitter = LaserEmitter(ctx.block, ctx.face).apply {
                runTaskTimer(Main.getPlugin(), 0, 1)
            }
            use()
        }, 1)
    }

    override fun unequip() {
        removeLaser()
    }

    override fun reset() {
        removeLaser()
    }

    override fun destroy() {
        removeLaser()
    }

    private fun removeLaser() {
        emitter?.remove()
    }

    inner class LaserEmitter(block: Block, face: BlockFace) : BukkitRunnable() {
        private val origin: Location
        private val center: Location
        private val end: Location
        private val offset: Vector
        private val laserUnitOffset: Vector
        private val length: Int
        private val color: Color
        private val world: World
        private val opponents: MutableList<BotBowsPlayer>

        init {
            var block = block
            origin = block.location.add(0.5, 0.5, 0.5)
            laserUnitOffset = face.getDirection()
            val mid1 = Vector(0.5, 0.5, 0.5).add(face.getDirection().multiply(0.1)) // positions near the center of the block to check for if it collides with. //TODO: find out if there needs to be more checks and not just 2
            val mid2 = mid1.add(face.getDirection().multiply(-0.2))
            var length = 0

            for (i in 0..99) {
                block = block.getRelative(face)

                if (block.type == Material.AIR || !block.isSolid || Util.isBlockMiddleTransparent(block.type)) {
                    length++
                    continue
                }
                val blockData = block.blockData
                if (block.type.isOccluding()) break

                if (blockData is Stairs || blockData is Slab || blockData is Wall) { // handling when the ray hits blocks that it should or shouldnt pass thru
                    val shape = block.blockData.getCollisionShape(block.location)
                    if (!shape.boundingBoxes.isEmpty()) {
                        val hit = shape.boundingBoxes.any { mid1 in it || mid2 in it }
                        if (hit) break
                    }
                }
                length++
            }
            end = block.location.add(0.5, 0.5, 0.5).subtract(face.getDirection().multiply(0.6))
            offset = face.getDirection().multiply(-length / 2)
            center = block.location.add(offset).add(0.5, 0.5, 0.5)
            offset.setX(abs(offset.getX())).setY(abs(offset.getY())).setZ(abs(offset.getZ())) // Its just abs() for every x y z

            this.length = length
            this.color = bp.team.dyeColor.color
            this.opponents = bp.team.oppositeTeam.players
            this.world = center.getWorld()
        }

        override fun run() {
            for (defender in opponents) {
                val proximity = defender.avatar.location.add(0.0, 1.0, 0.0).subtract(center)
                if (abs(proximity.x) < offset.getX() + 0.5
                    && abs(proximity.y) < offset.getY() + 1
                    && abs(proximity.z) < offset.getZ() + 0.5
                ) {
                    defender.damage(DamageContext.Player(DamageType.Player.LASER, bp))
                    registerSuccess()
                }
            }
            val loc = origin.clone()
            loc.add(laserUnitOffset.clone().multiply(0.5))
            repeat (length) { // laser ray
                world.spawnParticle( // thick laser mist with the same color as the team who placed it
                    Particle.DUST,
                    loc,
                    1,
                    laserUnitOffset.getX(),
                    laserUnitOffset.getY(),
                    laserUnitOffset.getZ(),
                    0.1,
                    DustOptions(color, 1f),
                    true
                )
                world.spawnParticle( // thin red ray where the laser light is
                    Particle.DUST,
                    loc,
                    4,
                    laserUnitOffset.getX(),
                    laserUnitOffset.getY(),
                    laserUnitOffset.getZ(),
                    0.1,
                    DustOptions(Color.RED, 0.2f),
                    true
                )
                loc.add(laserUnitOffset)
            }
            world.spawnParticle( // large red area where the laser ray hits
                Particle.DUST,
                end,
                1,
                0.0,
                0.0,
                0.0,
                1.0,
                DustOptions(Color.RED, 1.5f),
                true
            )
        }

        fun remove() {
            cancel()
            origin.block.type = Material.AIR
        }
    }
}
