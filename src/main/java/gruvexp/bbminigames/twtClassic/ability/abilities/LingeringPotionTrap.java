package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LingeringPotionTrap extends Ability implements AbilityTrigger.OnLingeringPotionUse {

    public static final int DURATION = 30; // how long the potion lingers on the ground
    public static final int EFFECT_DURATION = 20; // how long you have the effect after stepping into it
    public static final int LINGERING_POTION_RADIUS = 3;

    private static final PotionEffectType[] EFFECTS = {
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.DARKNESS,
            PotionEffectType.UNLUCK
    };

    private static final Map<PotionEffectType, Color> EFFECT_COLORS = Map.of(
            PotionEffectType.SLOWNESS, Color.fromRGB(90, 90, 255),
            PotionEffectType.LEVITATION, Color.fromRGB(255, 255, 255),
            PotionEffectType.BLINDNESS, Color.fromRGB(0, 0, 0),
            PotionEffectType.UNLUCK, Color.fromRGB(128, 100, 32)
    );

    protected static HashMap<AreaEffectCloud, BotBowsPlayer> cloudOwners = new HashMap<>();

    public LingeringPotionTrap(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LINGERING_POTION);
    }

    public static BotBowsPlayer getCloudOwner(AreaEffectCloud cloud) {
        return cloudOwners.get(cloud);
    }

    public static void giveRandomEffect(ThrownPotion thrownPotion) {
        PotionEffectType randomEffect = EFFECTS[BotBows.RANDOM.nextInt(EFFECTS.length)];
        Color potionColor = EFFECT_COLORS.getOrDefault(randomEffect, Color.GRAY);

        ItemStack potion = thrownPotion.getItem();
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.clearCustomEffects();
        meta.addCustomEffect(new PotionEffect(randomEffect, EFFECT_DURATION * 20 * 4, 2), true); // must *=4 the effect to counteract mojangs *=0.25 (bc its an area effect cloud)
        meta.setColor(potionColor);
        potion.setItemMeta(meta);
        thrownPotion.setItem(potion);
    }

    public void addSizeIncreaseAreaEffect(Location loc) {
        BotBowsPlayer throwerBp = bp;
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter++ >= DURATION * 10) {
                    cancel();
                    return;
                }

                for (Entity entity : loc.getWorld().getNearbyEntities(loc, LINGERING_POTION_RADIUS, 1, LINGERING_POTION_RADIUS)) {
                    BotBowsPlayer bp = BotBows.getBotBowsPlayer(entity.getUniqueId());
                    if (bp == null) continue;
                    if (bp.getTeam() == throwerBp.getTeam()) continue; // dont affect team of thrower

                    bp.getEffectManager().applyScale(
                            PlayerEffectManager.ScaleSource.GROW_TRAP,
                            1.5,
                            PlayerEffectManager.ScalePriority.NORMAL,
                            (long) (EFFECT_DURATION * 20)
                    );
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 2L);
    }

    @Override
    public void reset() {
        cloudOwners.keySet().forEach(Entity::remove);
    }

    @Override
    public void destroy() {
        cloudOwners.keySet().forEach(Entity::remove);
    }

    @Override
    public void onSplash(LingeringPotionSplashEvent e) {
        AreaEffectCloud cloud = e.getAreaEffectCloud();
        cloud.setReapplicationDelay(EFFECT_DURATION * 10);
        cloudOwners.put(cloud, bp);

        ThrownPotion potion = e.getEntity();
        boolean hasUnluck = potion.getEffects().stream()
                .anyMatch(effect -> effect.getType() == PotionEffectType.UNLUCK);
        if (hasUnluck) {
            addSizeIncreaseAreaEffect(potion.getLocation());
        }
    }

    @Override
    public void onCloudApply(AreaEffectCloudApplyEvent e) {
        PotionEffect cloudEffect = e.getEntity().getCustomEffects().getFirst();
        PotionEffectType effectType = cloudEffect.getType();
        long glowDuration = cloudEffect.getDuration() / 4; // only get 25% duration from the area effect cloud

        Iterator<LivingEntity> it = e.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity entity = it.next();
            BotBowsPlayer affectedBp = BotBows.getBotBowsPlayer(entity.getUniqueId());
            if (affectedBp == null) continue;
            if (affectedBp.getTeam() == bp.getTeam()) { // dont affect team of thrower
                it.remove();
                continue;
            }
            onEffectReceive(affectedBp, effectType, glowDuration);
        }
    }

    private void onEffectReceive(BotBowsPlayer affectedBp, PotionEffectType effectType, long glowDuration) {
        affectedBp.getEffectManager().applyGlow(
                PlayerEffectManager.GlowSource.DEBUFF,
                glowDuration,
                NamedTextColor.GOLD,
                10
        );
        String effectName = effectType == PotionEffectType.UNLUCK ? "GROWING" : effectType.getKey().value();
        affectedBp.lobby.messagePlayers(Component.text("", BotBows.lighten(bp.getTeamColor(), 0.5))
                .append(affectedBp.getName())
                .append(Component.text(" took a bath in "))
                .append(bp.getName())
                .append(Component.text("'s lingering potion cloud and got "))
                .append(Component.text(effectName, NamedTextColor.DARK_RED)));
    }
}
