package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class BabyPotion extends PotionAbility {

    public static int DURATION = 10;
    public static int AMPLIFIER = 4;

    public BabyPotion(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.BABY_POTION);
    }

    @Override
    public void applyPotionEffect(Set<BotBowsPlayer> players) {

        bp.avatar.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, AMPLIFIER));
        bp.getEffectManager().applyScale(
                PlayerEffectManager.ScaleSource.BABY_POTION,
                0.66,
                PlayerEffectManager.ScalePriority.NORMAL,
                DURATION * 20L
        );
        players.forEach(bp -> {
            bp.avatar.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 15, 4));
            bp.getEffectManager().applyScale(
                    PlayerEffectManager.ScaleSource.BABY_POTION,
                    0.75,
                    PlayerEffectManager.ScalePriority.NORMAL,
                    DURATION * 15L
            );
        }); // 75% of the effect will be given to other players on the team
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
