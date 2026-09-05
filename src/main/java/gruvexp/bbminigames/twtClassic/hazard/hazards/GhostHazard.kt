package gruvexp.bbminigames.twtClassic.hazard.hazards

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame
import gruvexp.bbminigames.twtClassic.hazard.Hazard
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import io.papermc.paper.entity.LookAnchor
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.random.Random

class GhostHazard : Hazard(HazardType.GHOST) {
    override fun init(players: Set<BotBowsPlayer>) {}

    override fun trigger(players: Set<BotBowsPlayer>) {
        for (bp in players) {
            val ghostMover = PlayerGhostMover(bp)
            ghostMover.runTaskTimer(Main.getPlugin(), 0L, 1L)
            hazardTimers[bp] = ghostMover

            Bukkit.getScheduler().runTaskLater(
                Main.getPlugin(),
                Runnable {
                    ghostMover.ascendGhost(bp.location)
                    val randomPitch = (0.8 + Random.nextDouble() * 0.4).toFloat()
                    Main.WORLD.playSound(
                        bp.team.spawnPos[0],
                        "minecraft:botbows.ghost_rise",
                        1.0f,
                        randomPitch
                    )
                    Main.WORLD.playSound(
                        bp.team.spawnPos[0],
                        "minecraft:botbows.ghost_rise",
                        1.0f,
                        randomPitch
                    )
                },
                60L
            ) // its 5 seconds delay, the ghost needs 2 seconds to ascend so it needs to ascend 3 seconds after starting to track the player
        }
        BotBows.setTimeSmooth(6000, 18000, 5)
    }

    override val announceMessage = HazardMessage("HAUNTED ARENA", "Stay in motion!", "HAUNTED ARENA")

    override val name = "Haunted Arena"

    override val description = arrayOf(
        Component.text("When there is ghost mode, you will get haunted"),
        Component.text("by your own ghost, and when you touch it,"),
        Component.text("you die")
    )

    override val actionDescription = "will be haunted by ghosts"

    override fun end() {
        hazardTimers.values.forEach { (it as PlayerGhostMover).descendGhost() }
        super.end()
        BotBows.setTimeSmooth(18000, 30000, 5)
    }

    class PlayerGhostMover(val bp: BotBowsPlayer) : BukkitRunnable() {
        val movementHistory = ArrayDeque<Location>(HISTORY_SIZE)
        private var isClose = false
        val ghost: ArmorStand

        init {
            ghost = spawnGhost()
        }

        override fun run() {
            val bpLoc = bp.location
            if (bp.isAlive && bp.lobby.botBowsGame!!.canMove) {
                movementHistory.add(bpLoc)
            }
            if (movementHistory.size < HISTORY_SIZE && bp.isAlive) return
            if (movementHistory.isEmpty()) return

            val ghostLoc = movementHistory.removeFirst()
            ghost.teleport(ghostLoc)
            if ((0..4).random() == 0) { // randomly gjør at ghostene blinker for å gjøre det litt scary
                val armor = bp.avatar.armor
                ghost.equipment.armorContents = arrayOf(
                    armor.boots,
                    armor.leggings,
                    armor.chestplate,
                    armor.helmet
                )
            } else {
                ghost.equipment.clear()
            }
            val distanceSquared = bpLoc.distanceSquared(ghostLoc)
            if (distanceSquared < 36) {
                ghost.lookAt(bpLoc, LookAnchor.EYES)
                if (!isClose) {
                    val randomPitch = 0.7f + Random.nextFloat() * 0.2f
                    bp.avatar.playSound(ghostLoc, "minecraft:botbows.ghost_approach", 1.0f, randomPitch)
                    isClose = true
                }
                if (distanceSquared < 9) {
                    ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD)
                    if (distanceSquared < 1) {
                        killPlayer(bp)
                    }
                } else {
                    ghost.setItem(EquipmentSlot.HAND, null)
                }
            } else {
                isClose = false
            }
        }

        private fun spawnGhost(): ArmorStand {
            val pLoc = bp.location
            val ghost = pLoc.getWorld().spawn(pLoc, ArmorStand::class.java)
            ghost.isInvisible = true
            ghost.setGravity(false)
            ghost.isMarker = true // ingen hitbox
            ghost.isSilent = true
            ghost.setArms(true)
            return ghost
        }

        private fun killPlayer(bp: BotBowsPlayer) {
            cancel()
            movementHistory.clear()
            val pLoc = bp.location
            val dir = pLoc.getDirection().multiply(-1).setY(0).normalize()

            val ghostLoc = pLoc.clone().add(dir) // 0.5 blocks behind the player
            ghostLoc.add(dir.crossProduct(Vector(0, 1, 0)).normalize().multiply(0.5)) // 0.5 blocks left of the player
            ghost.teleport(ghostLoc)

            ghost.setRotation(pLoc.yaw, pLoc.pitch)
            ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD_NETHERITE)
            bp.avatar.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false))

            object : BukkitRunnable() {
                val oldLocation: Location = pLoc
                var ticks: Int = 0
                val game: BotBowsGame? = bp.lobby.botBowsGame
                override fun run() {
                    if (game == null) { // if the game ended before the animation completed, then stop
                        cancel()
                        descendGhost(ghostLoc)
                        return
                    }
                    if (!bp.isAlive || ticks >= 40) {
                        cancel()
                        bp.damage(DamageContext.Environment(DamageType.Environment.GHOST))
                        descendGhost(ghostLoc)
                        return
                    }
                    oldLocation.yaw = bp.location.yaw
                    oldLocation.pitch = bp.location.pitch

                    bp.teleport(oldLocation)
                    ticks++
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L)
        }

        fun descendGhost() {
            descendGhost(ghost.location)
        }

        private fun descendGhost(ghostLoc: Location) {
            cancel()
            val blocks = -2.0
            val totalTicks = 2 * 20
            val Δy = blocks / totalTicks
            object : BukkitRunnable() {
                // slowly moves the ghost into the ground
                var ticks: Int = 0
                override fun run() {
                    if (ticks >= totalTicks) {
                        cancel()
                        ghost.remove() // the ghost is in the ground and can be removed
                        return
                    }
                    ghostLoc.add(0.0, Δy, 0.0)
                    ghost.teleport(ghostLoc)
                    ticks++
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L)
        }

        fun ascendGhost(pLoc: Location) {
            val blocks = 2.0
            val totalTicks = 2 * 20
            val Δy = blocks / totalTicks
            ghost.teleport(pLoc.subtract(0.0, blocks, 0.0))
            object : BukkitRunnable() {
                // slowly moves the ghost out of the ground
                var ticks: Int = 0
                override fun run() {
                    if (ticks >= totalTicks) {
                        cancel()
                        return
                    }
                    pLoc.add(0.0, Δy, 0.0)
                    ghost.teleport(pLoc)
                    ticks++
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L)
        }
    }

    companion object {
        private const val HISTORY_SIZE = 5 * 20 // 5s

        private val GHOST_SWORD get() = ItemStack(Material.IRON_SWORD)
            .apply { addEnchantment(Enchantment.SHARPNESS, 4) }

        private val GHOST_SWORD_NETHERITE get() = ItemStack(Material.NETHERITE_SWORD)
            .apply { addEnchantment(Enchantment.SHARPNESS, 4) }
    }
}
