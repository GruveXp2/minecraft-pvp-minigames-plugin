package gruvexp.bbminigames.twtClassic.hazard.hazards

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.hazard.Hazard
import gruvexp.bbminigames.twtClassic.hazard.HazardChance
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable

class EarthquakeHazard : Hazard(HazardType.EARTHQUAKE) {
    var anvilLocations = mutableSetOf<Location>()

    override fun init(players: Set<BotBowsPlayer>) {
        if (chance == HazardChance.DISABLED) return
        for (bp in players) {
            val bar = BossBar.bossBar(
                Component.text("Anvil timer", NamedTextColor.GOLD),
                0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.NOTCHED_6
            )
            bp.avatar.initHazardBar(HazardType.EARTHQUAKE, bar)
        }
    }

    override fun trigger(players: Set<BotBowsPlayer>) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            for (bp in players) {
                val earthQuakeTimer = PlayerEarthQuakeTimer(bp)
                earthQuakeTimer.runTaskTimer(Main.getPlugin(), 0L, 2L)
                hazardTimers[bp] = earthQuakeTimer
            }
        }, 5 * 20L)
    }

    override val announceMessage = HazardMessage("EARTHQUAKE INCOMING!", "Stay above ground!", "EARTHQUAKE INCOMING")

    override val name = "Earthquakes"

    override val description = arrayOf(
        Component.text("When there is an earthwuake, you will get hit by"),
        Component.text("stones if you go underground"),
        Component.text("for more than 5 seconds")
    )

    override val actionDescription = "will have storms"

    override fun end() {
        super.end()
        for (anvilLocation in anvilLocations) {
            val block = anvilLocation.block
            if (block.type == Material.ANVIL) {
                anvilLocation.block.type = Material.AIR
            }
        }
        anvilLocations.clear()
    }

    inner class PlayerEarthQuakeTimer(val bp: BotBowsPlayer) : BukkitRunnable() {
        var time: Int = 0

        private val isPlayerUnderground: Boolean
            get() {
                val pLoc = bp.location

                if (pLoc.y >= GROUND_LEVEL) return false

                val pBlock = pLoc.block
                return (pLoc.blockY + 2..UPPER_BOUND).all { y ->
                    pBlock.getRelative(BlockFace.UP, y - pLoc.blockY).type != Material.AIR
                }
            }

        override fun run() { // annehver tick = 10Hz
            if (!bp.isAlive) return  // if the player is dead, dont do anything

            if (isPlayerUnderground) {
                if (time < SECONDS * 40) { // 40 = run().frekvens*hvor_mye
                    time += 4 // tida går opp 4x så kjapt som når cooldownen går ned. Altså går tida opp 1s/s
                    if (time >= SECONDS * 40) {
                        bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, 1f)
                    } else {
                        bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, time / (SECONDS * 40f))
                    }
                } else {
                    val fallingAnvil = Main.WORLD.spawnFallingBlock(bp.location.add(0.0, 3.9, 0.0), Material.ANVIL.createBlockData())
                    fallingAnvil.setHurtEntities(true)
                    fallingAnvil.dropItem = false
                    time = 0 // resetting
                    bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, 0f)
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
                        bp.damage(DamageContext.Environment(DamageType.Environment.EARTHQUAKE))
                    }, 20L)
                    val anvilLoc = bp.location.toBlockLocation()
                    while (anvilLoc.block.type == Material.AIR) {
                        anvilLoc.subtract(0.0, 1.0, 0.0)
                    }
                    anvilLoc.add(0.0, 1.0, 0.0)
                    anvilLocations.add(anvilLoc)
                }
            } else {
                if (time > 0) {
                    time-- // cooldownen går ned 0.25s/s
                    bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, time / (SECONDS * 40f))
                }
            }
        }
    }
    companion object {
        const val GROUND_LEVEL: Int = 22
        const val UPPER_BOUND: Int = 29
        const val SECONDS: Int = 6 // hvor lenge man kan stå før Einstein kommer p
    }
}
