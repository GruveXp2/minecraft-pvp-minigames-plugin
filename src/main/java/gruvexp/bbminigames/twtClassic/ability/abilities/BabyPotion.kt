package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.PotionAbility
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BabyPotion(bp: BotBowsPlayer, hotBarSlot: Int) : PotionAbility(bp, hotBarSlot, AbilityType.BABY_POTION) {

    public override fun applyPotionEffect(players: Set<BotBowsPlayer>) {
        bp.avatar.addPotionEffect(PotionEffect(PotionEffectType.SPEED, DURATION * 20, AMPLIFIER))
        bp.effectManager.applyScale(
            PlayerEffectManager.ScaleSource.BABY_POTION,
            0.66,
            PlayerEffectManager.ScalePriority.NORMAL,
            DURATION * 20L
        )
        registerSuccess()
        players.forEach {
            it.avatar.addPotionEffect(PotionEffect(PotionEffectType.SPEED, DURATION * 15, 4))
            it.effectManager.applyScale(
                PlayerEffectManager.ScaleSource.BABY_POTION,
                0.75,
                PlayerEffectManager.ScalePriority.NORMAL,
                DURATION * 15L
            )
            registerSuccess()
        } // 75% of the effect will be given to other players on the team
    }

    override val effectName = "Baby"

    override val effectDuration: Int = (DURATION * 0.75).toInt()

    companion object {
        var DURATION: Int = 10
        var AMPLIFIER: Int = 4
    }
}
