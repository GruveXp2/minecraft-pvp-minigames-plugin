package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager;
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam;
import net.kyori.adventure.text.format.NamedTextColor;

public class Radar extends Ability {

    public static final int DURATION = 10 * 20; // ticks
    public static final int BLINK_PERIOD = 20; // ticks

    public Radar(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.RADAR);
    }

    @Override
    public void use() {
        super.use();
        BotBowsTeam opponentTeam = bp.getTeam().getOppositeTeam();
        opponentTeam.getPlayers().forEach(bp -> bp.getEffectManager().applyGlow(PlayerEffectManager.GlowSource.RADAR, (long) DURATION, NamedTextColor.YELLOW, BLINK_PERIOD));
        CreeperTrap.glowCreepers(opponentTeam, Radar.DURATION);
    }
}
