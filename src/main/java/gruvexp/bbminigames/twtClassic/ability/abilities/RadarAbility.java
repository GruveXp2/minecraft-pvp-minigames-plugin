package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class RadarAbility extends Ability {

    public static final int DURATION = 4; // seconds

    public RadarAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.RADAR);
    }

    @Override
    public void use() {
        super.use();
        bp.useRadarAbility();
    }
}
