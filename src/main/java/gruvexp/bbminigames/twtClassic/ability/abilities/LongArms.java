package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;

public class LongArms extends Ability implements AbilityTrigger.OnMelee {
    public LongArms(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LONG_ARMS);
    }

    @Override
    public void trigger(AbilityContext.Melee ctx) {
        use();
        ctx.defender().handleHit(Component.text(" was long-slapped by "), bp);
    }
}
