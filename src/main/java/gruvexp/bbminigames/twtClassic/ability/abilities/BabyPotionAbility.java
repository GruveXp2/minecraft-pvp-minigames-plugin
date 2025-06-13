package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class BabyPotionAbility extends PotionAbility {

    public static int DURATION = 10;
    public static int AMPLIFIER = 4;

    public BabyPotionAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot, AbilityType.BABY_POTION);
    }

    @Override
    public void applyPotionEffect(Set<Player> players) {

        players.forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 15, 4))); // 75% of the duration will be given to other players on the team
        bp.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, AMPLIFIER));
        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i == 10) {
                    this.cancel();
                }
                players.forEach(p -> p.getAttribute(Attribute.SCALE).setBaseValue(1.0 - 0.2/10 * i));
                bp.player.getAttribute(Attribute.SCALE).setBaseValue(1.0 - 0.34/10 * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i == 20) {
                    this.cancel();
                }
                players.forEach(p -> p.getAttribute(Attribute.SCALE).setBaseValue(0.8 + 0.2/20 * i));
                bp.player.getAttribute(Attribute.SCALE).setBaseValue(0.66 + 0.34/20 * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 200L, 1L);
    }
}
