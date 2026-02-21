package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class BabyPotion extends PotionAbility {

    public static int DURATION = 10;
    public static int AMPLIFIER = 4;

    public BabyPotion(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot, AbilityType.BABY_POTION);
    }

    @Override
    public void applyPotionEffect(Set<BotBowsPlayer> players) {

        players.forEach(p -> p.avatar.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 15, 4))); // 75% of the duration will be given to other players on the team
        bp.avatar.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, AMPLIFIER));
        bp.avatar.growSize(0.66, 10);
        bp.avatar.growSize(1, 20, DURATION * 20);
    }

    @Override
    protected String getEffectName() {
        return "Baby";
    }

    @Override
    protected int getEffectDuration() {
        return (int) (DURATION * 0.75);
    }
}
