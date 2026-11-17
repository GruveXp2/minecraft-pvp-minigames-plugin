package dev.gruve.minigames.botbows.ability.abilities

import dev.gruve.minigames.api.ability.AbilityContext.Melee
import dev.gruve.minigames.api.ability.AbilityTrigger.OnMelee
import dev.gruve.minigames.api.damage.DamageContext
import dev.gruve.minigames.api.damage.DamageType
import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.Ability
import dev.gruve.minigames.botbows.ability.AbilityType

class CoolRod(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.COOL_ROD), OnMelee {
    override fun trigger(ctx: Melee) {
        use()
        val successful =  ctx.defender.damage(DamageContext.Player(DamageType.Player.COOL_ROD, bp))
        if (successful) registerSuccess()
    }
}
