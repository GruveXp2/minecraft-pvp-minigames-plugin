package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class Radar extends Ability {

    public static final int DURATION = 4; // seconds

    public Radar(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.RADAR);
    }

    @Override
    public void use() {
        super.use();
        bp.useRadarAbility();
    }
}
