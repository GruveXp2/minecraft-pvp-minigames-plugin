package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class EnderPearlAbility extends Ability {
    public EnderPearlAbility(BotBowsPlayer player) {
        super(player);
        this.type = AbilityType.ENDER_PEARL;
        this.maxCooldown = 15;
    }

    @Override
    public AbilityType getType() {
        return type;
    }
}
