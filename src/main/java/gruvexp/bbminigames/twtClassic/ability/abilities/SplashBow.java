package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class SplashBow extends Ability implements AbilityTrigger.OnLaunch, AbilityTrigger.OnProjectileHit {

    public static final double BLAST_RADIUS = 3.0;

    public static HashMap<Arrow, BukkitTask> activeArrows = new HashMap<>();

    public SplashBow(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.SPLASH_BOW);
        bp.avatar.setItem(18, new ItemStack(Material.ARROW, 64));
    }

    public static void handleArrowHit(Player attacker, Location hitLoc) {
        Color attackerTeamColor = BotBows.getLobby(attacker).getBotBowsPlayer(attacker).getTeam().dyeColor.getColor();
        attacker.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, hitLoc, 5, BLAST_RADIUS /4, BLAST_RADIUS /4, BLAST_RADIUS /4, 5);
        attacker.getWorld().spawnParticle(Particle.DUST, hitLoc, 1000, 2, 2, 2, 0.4, new Particle.DustOptions(attackerTeamColor, 5));  // Red color
        for (Entity entity : attacker.getWorld().getNearbyEntities(hitLoc, BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS, entity -> entity instanceof Player)) {
            Player p = (Player) entity;
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) continue;
            if (lobby != BotBows.getLobby(attacker)) continue;
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            if (!bp.isAlive()) continue;

            bp.handleHit(Component.text(" was splash bowed by "), lobby.getBotBowsPlayer(attacker));
        }
    }

    @Override
    public void unequip() {
        bp.player.getInventory().remove(Material.ARROW);
    }

    @Override
    public void onLaunch(AbilityContext.Launch ctx) {
        if (ctx.projectile() instanceof Arrow arrow) {
            use();
            arrow.setColor(Color.RED);
            BukkitTask arrowTrail = new SplashBow.SplashArrowTrailGenerator(arrow, bp.getTeam().dyeColor.getColor())
                    .runTaskTimer(Main.getPlugin(), 1L, 1L);
            arrow.getVelocity().multiply(0.5f);
            activeArrows.put(arrow, arrowTrail);
            arrow.setMetadata("botbows_ability", new FixedMetadataValue(Main.getPlugin(), this));
        } else {
            throw new IllegalArgumentException("Splash bow tried to fire something that wasnt an arrow");
        }
    }

    @Override
    public void onHit(ProjectileHitEvent e) {
        Arrow arrow = (Arrow) e.getEntity();
        Player shooter = (Player) arrow.getShooter();
        Location hitLoc;
        if (e.getHitEntity() != null) {
            hitLoc = e.getHitEntity().getLocation();
        } else {
            hitLoc = e.getHitBlock().getLocation();
        }
        handleArrowHit(shooter, hitLoc);
        activeArrows.get(arrow).cancel();
        activeArrows.remove(arrow);
        arrow.remove();
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
            arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 5, 0.1, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.RED, 3), true);
            arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 20, 0.5, 0.5, 0.5, 0.4, new Particle.DustOptions(color, 2), true);
            arrow.getVelocity().add(new Vector(0, 0.03, 0));
        }
    }
}
