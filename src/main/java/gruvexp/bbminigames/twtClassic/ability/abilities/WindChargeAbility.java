package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class WindChargeAbility extends Ability {
    public WindChargeAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.WIND_CHARGE;
        this.maxCooldown = 15;
    }
}
