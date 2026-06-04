package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.min

class SneakManager(private val avatar: BotBowsAvatar) {
    companion object {
        const val CROUCH_LIMIT: Int = 10 // sec
    }

    private var sneakCooldown: SneakCooldown

    init {
        sneakCooldown = SneakCooldown()
        sneakCooldown.runTaskTimer(Main.getPlugin(), 0L, 1L)
    }

    val isSneakingExhausted: Boolean
        get() = sneakCooldown.isExhausted

    fun destroy() {
        sneakCooldown.cancel()
    }

    private inner class SneakCooldown : BukkitRunnable() {
        var time: Int = 20
        var isExhausted: Boolean = false

        override fun run() {
            val isAlive = avatar.botBowsPlayer.isAlive
            if (!isAlive) {
                time -= min(time, 10)
            } else if (avatar.isSneaking()) {
                if (time < CROUCH_LIMIT * 20 - 1) {
                    time += 2
                } else {
                    isExhausted = true
                }
            } else if (time == 0) {
                isExhausted = false
            } else {
                time -= 1
            }
            val progress = when {
                time == 0 -> 0f
                time >= CROUCH_LIMIT * 20 - 1 -> 1f
                else -> time.toFloat() / (CROUCH_LIMIT * 20)
            }
            avatar.updateSneakStamina(progress)
        }
    }
}
