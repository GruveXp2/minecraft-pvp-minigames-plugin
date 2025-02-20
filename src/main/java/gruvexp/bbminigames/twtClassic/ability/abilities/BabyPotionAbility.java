package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.stream.Collectors;

public class BabyPotionAbility extends Ability {

    public static int DURATION = 10;
    public static int RADIUS = 4;

    protected BabyPotionAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.BABY_POTION;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    public void use() {
        super.use();
        Set<Player> players = Main.WORLD.getNearbyEntities(player.player.getLocation(), RADIUS, RADIUS, RADIUS, entity -> entity instanceof Player p)
                .stream().map(p -> (Player) p)
                .filter(p -> BotBows.getLobby(p) != null)
                .filter(p -> BotBows.getLobby(p).getBotBowsPlayer(p).getTeam() == player.getTeam())
                .collect(Collectors.toSet());

        players.forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 15, 4))); // 75% of the duration will be given to other players on the team

        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i == 10) {
                    this.cancel();
                }
                players.forEach(p -> p.getAttribute(Attribute.SCALE).setBaseValue(1.0 - 0.5/10 * i));
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
                players.forEach(p -> p.getAttribute(Attribute.SCALE).setBaseValue(0.5 + 0.5/20 * i));
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 200L, 1L);
    }
}
