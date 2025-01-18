package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class SplashBowAbility extends Ability {

    public static final double SPLASH_RADIUS = 3.0;

    protected SplashBowAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.SPLASH_BOW;
        this.baseCooldown = type.getBaseCooldown();
    }
}
