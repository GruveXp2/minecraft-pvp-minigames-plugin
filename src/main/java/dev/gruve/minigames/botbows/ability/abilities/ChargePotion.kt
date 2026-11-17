package dev.gruve.minigames.botbows.ability.abilities

import dev.gruve.minigames.Main
import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.AbilityType
import dev.gruve.minigames.botbows.ability.PotionAbility
import org.bukkit.Bukkit

class ChargePotion(bp: BotBowsPlayer, hotBarSlot: Int) : PotionAbility(bp, hotBarSlot, AbilityType.CHARGE_POTION) {
    init {
        baseCooldown = type.baseCooldown
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

        Bukkit.getScheduler().runTaskLater(Main.plugin, Runnable {
            bp.setAbilityCooldownTickRate(20)
            players.forEach { it.setAbilityCooldownTickRate(20) }
        }, 20L * DURATION)
    }

    override val effectName = "Charge"

    override val effectDuration: Int = DURATION * 3 / 4

    companion object {
        const val DURATION: Int = 20
    }
}
