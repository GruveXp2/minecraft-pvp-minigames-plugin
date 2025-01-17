package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;

public class RadarAbility extends Ability {
    public RadarAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.RADAR;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    public void use() {
        super.use();
        player.useRadarAbility();
    }
}
