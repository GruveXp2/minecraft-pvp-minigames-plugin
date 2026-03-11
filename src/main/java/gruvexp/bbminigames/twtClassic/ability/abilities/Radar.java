package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

public class Radar extends Ability {

    public static final int DURATION = 4; // seconds
    public static final int BLINK_PERIOD = 20; // ticks

    public Radar(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.RADAR);
    }

    @Override
    public void use() {
        super.use();
        BotBowsTeam opponentTeam = bp.getTeam().getOppositeTeam();
        opponentTeam.glow(Radar.DURATION);
        CreeperTrap.glowCreepers(opponentTeam, Radar.DURATION);
        final int blinks = Radar.DURATION * 20 / Radar.BLINK_PERIOD;
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                step++;
                opponentTeam.setGlowColor(NamedTextColor.YELLOW, Radar.BLINK_PERIOD/2);
                if (step == blinks) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 0, Radar.BLINK_PERIOD);
    }
}
