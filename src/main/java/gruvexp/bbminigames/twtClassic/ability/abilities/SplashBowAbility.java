package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;

public class SplashBowAbility extends Ability {

    public static final double SPLASH_RADIUS = 3.0;

    public SplashBowAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot);
        this.type = AbilityType.SPLASH_BOW;
        this.baseCooldown = type.getBaseCooldown();
        bp.player.getInventory().setItem(18, new ItemStack(Material.ARROW, 64));
    }

    public static void handleArrowHit(Player attacker, Location hitLoc) {
        Color attackerTeamColor = BotBows.getLobby(attacker).getBotBowsPlayer(attacker).getTeam().dyeColor.getColor();
        Main.WORLD.spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 5, SPLASH_RADIUS/4, SPLASH_RADIUS/4, SPLASH_RADIUS/4, 5);
        Main.WORLD.spawnParticle(Particle.DUST, hitLoc, 1000, 2, 2, 2, 0.4, new Particle.DustOptions(attackerTeamColor, 5));  // Red color
        for (Entity entity : Main.WORLD.getNearbyEntities(hitLoc, SPLASH_RADIUS, SPLASH_RADIUS, SPLASH_RADIUS, entity -> entity instanceof Player)) {
            Player p = (Player) entity;
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            if (lobby != BotBows.getLobby(attacker)) return;
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            bp.handleHit(lobby.getBotBowsPlayer(attacker), Component.text(" was splash bowed by "));
        }
    }

    public static class SplashArrowTrailGenerator extends BukkitRunnable {

        private final Arrow arrow;
        private final Color color;

        public SplashArrowTrailGenerator(Arrow arrow, Color color) {
            this.arrow = arrow;
            this.color = color;
        }

        @Override
        public void run() {
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 5, 0.1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.RED, 3), true);
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 20, 0.5, 0.5, 0.5, 0.4, new Particle.DustOptions(color, 2), true);
            arrow.getVelocity().add(new Vector(0, 0.03, 0));
        }
    }
}
