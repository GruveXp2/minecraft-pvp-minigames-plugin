package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class KarmaPotion extends PotionAbility {

    public static final int DURATION = 20;

    public KarmaPotion(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.KARMA_POTION);
    }

    @Override
    protected void applyPotionEffect(Set<BotBowsPlayer> players) {
        bp.setKarmaEffect(true);
        players.forEach(p -> p.setKarmaEffect(true));

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), _ -> bp.setKarmaEffect(false), 20L * DURATION);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), _ -> players.forEach(p -> p.setKarmaEffect(false)), 15L * DURATION);
    }

    @Override
    protected String getEffectName() {
        return "Karma";
    }

    @Override
    protected int getEffectDuration() {
        return (int) (DURATION * 0.75);
    }
}
