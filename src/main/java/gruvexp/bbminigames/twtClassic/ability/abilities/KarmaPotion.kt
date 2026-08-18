package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.PotionAbility
import org.bukkit.Bukkit

class KarmaPotion(bp: BotBowsPlayer, hotBarSlot: Int) : PotionAbility(bp, hotBarSlot, AbilityType.KARMA_POTION) {
    override fun applyPotionEffect(players: Set<BotBowsPlayer>) {
        bp.setKarmaEffect(true)
        players.forEach { it.setKarmaEffect(true) }

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { bp.setKarmaEffect(false) }, 20L * DURATION)
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { players.forEach { it.setKarmaEffect(false) } }, 15L * DURATION)
    }

    override val effectName: String = "Karma"

    override val effectDuration: Int = (DURATION * 0.75).toInt()

    companion object {
        const val DURATION: Int = 20 // seconds
        const val KARMA_DURATION: Int = 10 // seconds
    }
}
