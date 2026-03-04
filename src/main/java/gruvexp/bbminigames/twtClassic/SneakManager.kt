package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar
import org.bukkit.scheduler.BukkitRunnable
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
            BotBows.debugMessage("\n========", TestCommand.test3)
            if (avatar.isSneaking()) {
                BotBows.debugMessage("sneaking", TestCommand.test3)
                if (time < CROUCH_LIMIT * 20 - 1) {
                    time += 2
                    val progress = time.toFloat() / (CROUCH_LIMIT * 20)
                    avatar.updateSneakStamina(progress)
                } else {
                    avatar.updateSneakStamina(1f)
                    isExhausted = true
                }
            } else if (time == 0) {
                BotBows.debugMessage("not sneaking, t=0", TestCommand.test3)
                isExhausted = false
                avatar.updateSneakStamina(0f)
            } else {
                BotBows.debugMessage("not sneaking, t>0", TestCommand.test3)
                time -= 1
                val progress = time.toFloat() / (CROUCH_LIMIT * 20)
                avatar.updateSneakStamina(progress)
            }
        }
    }
}
