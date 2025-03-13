package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class LongArmsAbility extends Ability {
    public LongArmsAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot);
        this.type = AbilityType.LONG_ARMS;
    }
}
