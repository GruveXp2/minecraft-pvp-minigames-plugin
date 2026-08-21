package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.api.ability.AbilityContext.Melee
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnMelee
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType

class LongArms(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.LONG_ARMS), OnMelee {
    override fun trigger(ctx: Melee) {
        use()
        ctx.defender.damage(DamageContext.Player(DamageType.Player.COOL_ROD, bp))
        registerSuccess()
    }
}
