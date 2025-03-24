package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class LingeringPotionAbility extends Ability {

    public static final int DURATION = 15;

    private static final PotionEffectType[] EFFECTS = {
            PotionEffectType.SLOWNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.LEVITATION,
            PotionEffectType.BLINDNESS
    };

    private static final Map<PotionEffectType, Color> EFFECT_COLORS = Map.of(
            PotionEffectType.SLOWNESS, Color.fromRGB(90, 90, 255),
            PotionEffectType.NAUSEA, Color.fromRGB(128, 0, 128),
            PotionEffectType.LEVITATION, Color.fromRGB(255, 255, 255),
            PotionEffectType.BLINDNESS, Color.fromRGB(0, 0, 0)
    );

    public LingeringPotionAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LINGERING_POTION);
    }

    public static void giveRandomEffect(ItemStack potion) {
        PotionEffectType randomEffect = EFFECTS[BotBows.RANDOM.nextInt(EFFECTS.length)];
        Color potionColor = EFFECT_COLORS.getOrDefault(randomEffect, Color.GRAY);

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta != null) {
            meta.clearCustomEffects();
            meta.addCustomEffect(new PotionEffect(randomEffect, DURATION * 20, 0), true);
            meta.setColor(potionColor);
            potion.setItemMeta(meta);
        }
    }
}
