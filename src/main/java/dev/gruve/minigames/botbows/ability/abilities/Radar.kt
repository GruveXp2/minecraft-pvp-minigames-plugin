package dev.gruve.minigames.botbows.ability.abilities

import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.Ability
import dev.gruve.minigames.botbows.ability.AbilityType
import dev.gruve.minigames.botbows.effect.PlayerEffectManager
import net.kyori.adventure.text.format.NamedTextColor

class Radar(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.RADAR) {
    override fun use() {
        super.use()
        val opponentTeam = bp.team.oppositeTeam
        opponentTeam.players.forEach {
            it.effectManager.applyGlow(
                PlayerEffectManager.GlowSource.RADAR,
                DURATION.toLong(),
                NamedTextColor.YELLOW,
                BLINK_PERIOD
            )
        }
        CreeperTrap.glowCreepers(opponentTeam, DURATION)
        registerSuccess()
    }

    companion object {
        const val DURATION: Int = 10 * 20 // ticks
        const val BLINK_PERIOD: Int = 20 // ticks
    }
}
