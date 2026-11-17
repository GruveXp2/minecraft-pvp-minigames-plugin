package dev.gruve.minigames.botbows.ability.abilities

import dev.gruve.minigames.Main
import dev.gruve.minigames.botbows.damage.DamageContext
import dev.gruve.minigames.botbows.damage.DamageType
import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.Ability
import dev.gruve.minigames.botbows.ability.AbilityType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class BubbleJet(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.BUBBLE_JET) {
    var riptideTask: BukkitRunnable? = null

    override fun use() {
        super.use()
        bp.setInvulnerable(true)
        if (riptideTask != null) return
        riptideTask = object : BukkitRunnable() {
            override fun run() {
                if (bp.isOnGround) {
                    bp.setInvulnerable(false)
                    cancel() // if the player is done riptiding and hitting the ground
                    riptideTask = null
                    return
                }
                bp.getNearbyPlayers(DAMAGE_RADIUS)
                    .filter { it.team != bp.team }
                    .forEach {
                        it.avatar.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false))
                        val successful = it.damage(DamageContext.Player(DamageType.Player.BUBBLE_JET, bp))
                        if (successful) registerSuccess()
                    }
            }
        }.apply { runTaskTimer(Main.plugin, 0L, 2L) }
    }

    companion object {
        const val DAMAGE_RADIUS: Double = 2.0
    }
}
