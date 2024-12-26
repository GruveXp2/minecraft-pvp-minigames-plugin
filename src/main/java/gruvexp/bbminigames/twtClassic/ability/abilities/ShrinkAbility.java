package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import org.bukkit.attribute.Attribute;

public class ShrinkAbility extends Ability {
    public ShrinkAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.baseCooldown = 20;
    }

    @Override
    public void use() {
        super.use();
        player.player.getAttribute(Attribute.SCALE).setBaseValue(.5);
    }
}
