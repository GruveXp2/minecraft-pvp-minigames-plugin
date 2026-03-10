package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.api.damage.DamageContext;
import gruvexp.bbminigames.api.damage.DamageType;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class LongArms extends Ability implements AbilityTrigger.OnMelee {
    public LongArms(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LONG_ARMS);
    }

    @Override
    public void trigger(AbilityContext.Melee ctx) {
        use();
        ctx.defender().damage(new DamageContext.Player(DamageType.Player.COOL_ROD, bp));
    }
}
