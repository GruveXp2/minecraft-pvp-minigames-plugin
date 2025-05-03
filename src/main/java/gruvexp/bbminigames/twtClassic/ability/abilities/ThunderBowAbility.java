package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.Menu;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ThunderBowAbility extends Ability {

    public static final ItemStack THUNDER_BOW = Menu.makeItem(Material.CROSSBOW, "thunder_bow", Component.text("ThunderBow"), Component.text("Shoots electric arrows"));
    public static final double SPLASH_RADIUS = 6.0;
    public static final int DURATION = 10; // seconds

    private boolean isActive = false;

    public ThunderBowAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.THUNDER_BOW);
    }

    @Override
    public void use() {
        super.use();
        isActive = true;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> isActive = false, 20L * DURATION);
    }

    public boolean isActive() {
        return isActive;
    }

    public static void handleArrowHitPlayer(BotBowsPlayer attacker, BotBowsPlayer defender) {
        defender.handleHit(Component.text(" was thunderbowed by "), attacker);
        Location hitLoc = defender.player.getLocation();
        Color attackerTeamColor = attacker.getTeam().dyeColor.getColor();
        for (Entity entity : Main.WORLD.getNearbyEntities(hitLoc, SPLASH_RADIUS, SPLASH_RADIUS, SPLASH_RADIUS, entity -> entity instanceof Player)) {
            Player p = (Player) entity;
            if (p == defender.player) return;
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            if (lobby != attacker.lobby) return;
            Main.WORLD.strikeLightningEffect(p.getLocation());
            createElectricArc(hitLoc, p.getLocation(), attackerTeamColor, 1.0);
            BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
            bp.handleHit(Component.text(" was electrobowed by "), attacker);
        }
    }

    public static void handleArrowHitBlock(Location hitLoc) {
        for (int i = 0; i < 10; i++) {
            int x = BotBows.RANDOM.nextInt(11) - 5; // -5 til 5
            int y = BotBows.RANDOM.nextInt(11) - 5;
            int z = BotBows.RANDOM.nextInt(11) - 5;
            Vector randomVec = new Vector(x, y, z);
            Location arcLoc = hitLoc.clone().add(randomVec);
            if (arcLoc.getBlock().getType() != Material.AIR) {
                createElectricArc(hitLoc, arcLoc, Color.AQUA, 2.0);
            }
        }
    }

    public static void createElectricArc(Location loc1, Location loc2, Color color, double frequencyMultiplier) {
        double length = loc1.distance(loc2);
        Vector diff = new Vector(
                loc2.getX() - loc1.getX(),
                loc2.getY() - loc1.getY(),
                loc2.getZ() - loc1.getZ()
        );
        int steps = (int) (length * frequencyMultiplier);
        List<Vector> locations = new ArrayList<>(steps + 1);
        Vector start = loc1.toVector();
        Vector end = loc2.toVector();
        locations.add(start);
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps; // Interpolation factor (0 to 1)
            double x = start.getX() + t * (end.getX() - start.getX());
            double y = start.getY() + t * (end.getY() - start.getY());
            double z = start.getZ() + t * (end.getZ() - start.getZ());
            Vector vecI = new Vector(x, y, z).add(getRandomPerpendicular(diff).multiply(Math.random()));
            locations.add(vecI);
        }
        locations.add(end);
        for (int i = 0; i < locations.size() - 1; i++) {
            Vector rayDiff = locations.get(i).multiply(-1).add(locations.get(i + 1)).multiply(0.1);
            for (int j = 0; j < 10; j++) {
                loc1.add(rayDiff);
                if (j % 2 == 0) Main.WORLD.spawnParticle(Particle.DUST, loc1, 1, 0.1, 0.1, 0.1, 10, new Particle.DustOptions(color, 0.8f));
                Main.WORLD.spawnParticle(Particle.DUST, loc1, 5, 0.05, 0.05, 0.05, 0.1, new Particle.DustOptions(Color.WHITE, 0.5f));
            }
        }
    }

    private static Vector getRandomPerpendicular(Vector diff) {
        Vector reference = new Vector(0, 1, 0);

        if (Math.abs(diff.getY()) > 0.99) { // If it's too close to the y-axis, switch reference
            reference = new Vector(1, 0, 0);
        }

        Vector perpendicular = diff.clone().crossProduct(reference).normalize();
        double angle = Math.random() * 2 * Math.PI;

        return perpendicular.rotateAroundAxis(diff, angle);
    }

    public static class ThunderArrowTrailGenerator extends BukkitRunnable {

        private final Arrow arrow;
        private final Color color;

        public ThunderArrowTrailGenerator(Arrow arrow, Color color) {
            this.arrow = arrow;
            this.color = color;
        }

        @Override
        public void run() {
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 5, 0.1, 0.1, 0.1, 0.5, new Particle.DustOptions(Color.WHITE, 1), true);
            Main.WORLD.spawnParticle(Particle.DUST, arrow.getLocation(), 5, 0.1, 0.1, 0.1, 0.3, new Particle.DustOptions(color, 0.5f), true);
            arrow.getVelocity().add(new Vector(0, 0.03, 0));
            Vector spark = getRandomPerpendicular(arrow.getVelocity()).multiply(1 + Math.random() * 2);
            Location sparkLocation = arrow.getLocation().add(spark);
            if (BotBows.RANDOM.nextInt(3) == 0 && sparkLocation.getBlock().getType() != Material.AIR) {
                createElectricArc(arrow.getLocation(), sparkLocation, Color.AQUA, 2.0);
            }
        }
    }
}
