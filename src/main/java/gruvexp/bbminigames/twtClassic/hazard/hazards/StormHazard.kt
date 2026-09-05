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
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable

class StormHazard : Hazard(HazardType.STORM) {
    override fun init(players: Set<BotBowsPlayer>) {
        if (chance == HazardChance.DISABLED) return
        for (bp in players) {
            val bar = BossBar.bossBar(
                Component.text("Lightning timer", NamedTextColor.AQUA),
                0f,
                BossBar.Color.BLUE,
                BossBar.Overlay.NOTCHED_6
            )
            bp.avatar.initHazardBar(HazardType.STORM, bar)
        }
    }

    override fun trigger(players: Set<BotBowsPlayer>) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            for (bp in players) {
                val stormTimer = PlayerStormTimer(bp)
                stormTimer.runTaskTimer(Main.getPlugin(), 0L, 2L)
                hazardTimers[bp] = stormTimer
            }
            Main.WORLD.isThundering = true
            Main.WORLD.setStorm(true)
            Main.WORLD.thunderDuration = 600 * 20 //10min
        }, 5 * 20L)
    }

    override fun getAnnounceMessage(): HazardMessage {
        return HazardMessage("STORM INCOMING!", "Seek shelter immediately!", "STORM INCOMING")
    }

    override fun getName() = "Storms"

    override fun getDescription(): Array<Component> {
        return arrayOf(
            Component.text("When there is a storm, you will get hit by"),
            Component.text("lightning if you stand in dirext exposure"),
            Component.text("to the sky for more than 5 seconds")
        )
    }

    override fun getActionDescription() = "will have storms"

    override fun end() {
        super.end()
        Main.WORLD.isThundering = false
        Main.WORLD.setStorm(false)
        Main.WORLD.clearWeatherDuration = 12000
    }

    class PlayerStormTimer(val bp: BotBowsPlayer) : BukkitRunnable() {
        var time: Int = 0

        private val isPlayerExposed: Boolean
            get() {
                val pLoc = bp.location
                if (pLoc.y < GROUND_LEVEL) {
                    return false
                }
                if (pLoc.y >= UPPER_BOUND) {
                    return true
                }

                val baseBlock = pLoc.block
                return (pLoc.blockY + 2..UPPER_BOUND).all { y -> // check all blocks above for air
                    baseBlock.getRelative(BlockFace.UP, y - pLoc.blockY).type == Material.AIR
                }
            }

        override fun run() { // annehver tick = 10Hz
            if (!bp.isAlive) return  // if the player is dead, dont do anything

            if (isPlayerExposed) {
                if (time < SECONDS * 40) { // 40 = run().frekvens*hvor_mye
                    time += 4 // tida går opp 4x så kjapt som når cooldownen går ned. Altså går tida opp 1s/s
                    if (time >= SECONDS * 40) {
                        bp.avatar.setHazardBarProgress(HazardType.STORM, 1f)
                    } else {
                        bp.avatar.setHazardBarProgress(HazardType.STORM, time / (SECONDS * 40f))
                    }
                } else {
                    Main.WORLD.strikeLightningEffect(bp.location)
                    time = 0 // resetting
                    bp.avatar.setHazardBarProgress(HazardType.STORM, 0f)
                    bp.damage(DamageContext.Environment(DamageType.Environment.LIGHTNING))
                }
            } else {
                if (time > 0) {
                    time-- // cooldownen går ned 0.25s/s
                    bp.avatar.setHazardBarProgress(HazardType.STORM, time / (SECONDS * 40f))
                }
            }
        }

        companion object {
            const val GROUND_LEVEL: Int = 22
            const val UPPER_BOUND: Int = 29
            const val SECONDS: Int = 6 // hvor lenge man kan stå før man blir tatt av lightning
        }
    }
}
