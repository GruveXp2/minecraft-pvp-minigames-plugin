package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class LingeringPotionTrap extends Ability {

    public static final int DURATION = 30;
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

    public LingeringPotionTrap(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LINGERING_POTION);
    }

    public static void giveRandomEffect(ThrownPotion thrownPotion) {
        PotionEffectType randomEffect = EFFECTS[BotBows.RANDOM.nextInt(EFFECTS.length)];
        Color potionColor = EFFECT_COLORS.getOrDefault(randomEffect, Color.GRAY);

        ItemStack potion = thrownPotion.getItem();
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.clearCustomEffects();
        meta.addCustomEffect(new PotionEffect(randomEffect, DURATION * 20, 2), true);
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

                for (Entity entity : loc.getWorld().getNearbyEntities(loc, LINGERING_POTION_RADIUS, 1, LINGERING_POTION_RADIUS, e -> e instanceof Player)) {
                    Player p = (Player) entity;
                    Lobby lobby = BotBows.getLobby(p);
                    if (lobby == null) return;
                    BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
                    if (bp.getTeam() == throwerBp.getTeam()) continue; // dont affect team of thrower

                    bp.growSize(20);
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 2L);
    }
}
