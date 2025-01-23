package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class FloatSpellAbility extends Ability {

    public static final int DURATION = 3;

    public FloatSpellAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.FLOAT_SPELL;
        this.baseCooldown = type.getBaseCooldown();
    }

    public void handleUsage(Chicken chicken) {
        for (Entity nearbyEntity : Main.WORLD.getNearbyEntities(chicken.getLocation(), 5, 5, 5, entity -> entity instanceof Player)) {
            Player p = (Player) nearbyEntity;
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) continue;
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            if (bp != player) { // kun på andre players
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, FloatSpellAbility.DURATION * 20, 1, false, false));
            }
        }
    }

    public static void animateChicken(Chicken chicken) {
        new BukkitRunnable() {
            int i = 1;
            final int TOTAL = DURATION * 20;
            @Override
            public void run() {
                if (i == TOTAL) {
                    this.cancel();
                    chicken.remove();
                }
                double value = 2 * Math.sin(Math.PI/6 * ((double) i /12 + 1));
                Main.getPlugin().getLogger().info("i = " + i + ", val: " + value);
                chicken.getAttribute(Attribute.SCALE).setBaseValue(value);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1L);
    }
}
