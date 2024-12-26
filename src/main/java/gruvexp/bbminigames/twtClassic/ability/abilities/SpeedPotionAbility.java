package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class SpeedPotionAbility extends Ability {
    public SpeedPotionAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.SPEED_POTION;
        this.maxCooldown = 25;
    }
}
