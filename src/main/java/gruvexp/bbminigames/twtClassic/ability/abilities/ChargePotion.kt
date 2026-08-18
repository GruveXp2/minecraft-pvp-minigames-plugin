package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.PotionAbility
import org.bukkit.Bukkit

class ChargePotion(bp: BotBowsPlayer, hotBarSlot: Int) : PotionAbility(bp, hotBarSlot, AbilityType.CHARGE_POTION) {
    init {
        this.baseCooldown = type.baseCooldown
    }

    override fun use() {
        super.use()
        bp.obtainWeaponAbilities()
    }

    override fun applyPotionEffect(players: Set<BotBowsPlayer>) {
        bp.setAbilityCooldownTickRate(10)
        players.forEach {
            it.setAbilityCooldownTickRate(13)
            it.obtainWeaponAbilities()
        }
        registerSuccess()

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            bp.setAbilityCooldownTickRate(20)
            players.forEach { it.setAbilityCooldownTickRate(20) }
            registerSuccess()
        }, 20L * DURATION)
    }

    override val effectName = "Charge"

    override val effectDuration: Int = (DURATION * 0.75).toInt()

    companion object {
        const val DURATION: Int = 20
    }
}
