package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SplashBowAbility extends Ability {

    public static final double SPLASH_RADIUS = 3.0;

    public SplashBowAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.SPLASH_BOW;
        this.baseCooldown = type.getBaseCooldown();
    }

    public static void handleArrowHit(Player attacker, Location hitLoc) {
        Main.WORLD.spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 30, SPLASH_RADIUS, SPLASH_RADIUS, SPLASH_RADIUS, 0.1);
        Main.WORLD.spawnParticle(Particle.DUST, hitLoc, 25, 3, 3, 3, 0.1, new Particle.DustOptions(Color.RED, 5));  // Red color
        for (Entity entity : Main.WORLD.getNearbyEntities(hitLoc, SPLASH_RADIUS, SPLASH_RADIUS, SPLASH_RADIUS, entity -> entity instanceof Player)) {
            Player p = (Player) entity;
            p.sendMessage("You are within 3 blocks of the arrow impact!");
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            if (lobby != BotBows.getLobby(attacker)) return;
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            bp.handleHit(lobby.getBotBowsPlayer(attacker));
        }
    }

    public static class ArrowTrailGenerator extends BukkitRunnable {

        private final Arrow arrow;

        public ArrowTrailGenerator(Arrow arrow) {
            this.arrow = arrow;
        }

        @Override
        public void run() {
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 5, 1, 1, 1, 0.1, new Particle.DustOptions(Color.RED, 1));  // Red color
        }
    }
}
