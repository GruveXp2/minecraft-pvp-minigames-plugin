package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
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
                    this.cancel() // if the player is done riptiding and hitting the ground
                    riptideTask = null
                    return
                }
                bp.getNearbyPlayers(DAMAGE_RADIUS).stream()
                    .filter { it.team != bp.team }
                    .forEach {
                        it.avatar.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false))
                        it.damage(DamageContext.Player(DamageType.Player.BUBBLE_JET, bp))
                        registerSuccess()
                    }
            }
        }.apply { runTaskTimer(Main.getPlugin(), 0L, 2L) }
    }

    companion object {
        const val DAMAGE_RADIUS: Double = 2.0
    }
}
