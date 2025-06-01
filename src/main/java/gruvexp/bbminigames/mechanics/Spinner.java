package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Spinner {

    private final Set<BlockDisplay> displays;
    private final Location centerLocation;
    private final float rotationSpeed;

    private float jaw = 0f;
    private final Set<Player> players = new HashSet<>();

    public Spinner(String displayTag, Location centerLocation, float rotationSpeed) {
        this.centerLocation = centerLocation;
        this.rotationSpeed = rotationSpeed;
        displays = new HashSet<>();
        for (Entity nearbyEntity : centerLocation.getNearbyEntities(2, 2, 2)) {
            if (!(nearbyEntity instanceof BlockDisplay display)) continue;
            if (!display.getScoreboardTags().contains(displayTag)) continue;

            displays.add(display);
            display.setRotation(0, 0);
        }
    }

    // the chunks close to this spinner, only check for players if players are in these chunks
    public Set<Chunk> getTickedChunks() {
        return Util.getChunksAround(centerLocation, 3);
    }

    public void checkProximity(Player p) {
        if (players.contains(p)) return;

        if (p.getLocation().distanceSquared(centerLocation) < 9) {
            players.add(p);
            Bukkit.broadcast(Component.text("A player came near"));
        }
    }

    public void tick() {
        jaw += rotationSpeed;
        if (jaw < 0) {
            jaw += 360;
        } else if (jaw > 360) {
            jaw = jaw % 360;
        }
        displays.forEach(display -> display.setRotation(jaw, 0));

        players.forEach(p -> {
            Location pLoc = p.getLocation();
            Vector relDir = centerLocation.clone().subtract(pLoc).toVector(); // retningsvektoren
            relDir.setY(0);
            double x = relDir.getX();
            double z = relDir.getZ();
            // polar form
            double r = Math.sqrt(x * x + z * z);
            double θ = Math.atan2(z, x);

            double pJaw = Math.toDegrees(θ) - 90;
            pJaw -= jaw; // relative to the rotation of the spinner
            // make into interval [0,90)
            pJaw = (pJaw + 720) % 90;
            if (rotationSpeed > 0 ? pJaw < 20 : pJaw > 70) { // they hit the spinner and will get pushed
                Bukkit.broadcast(Component.text("A player pushed(" + (int)pJaw + "), "));
                double divide = 5 + (rotationSpeed > 0 ? pJaw : 90 - pJaw) * 7;
                Vector push = relDir.crossProduct(new Vector(0, -rotationSpeed, 0)).multiply(r/(divide)); // retning x up||down = vel from hitting the bl8d

                Vector v = p.getVelocity();
                Vector pushDir = push.clone().normalize();
                double vAlongPushDir = v.dot(pushDir);
                v.subtract(pushDir.multiply(vAlongPushDir)).add(push); // the part of v in the direction pushed gets completly replaced with the push value
                p.setVelocity(v);
            } else if (rotationSpeed > 0 ? pJaw > 70 : pJaw < 20) { // they hit the spinner and will get pushed
                Bukkit.broadcast(Component.text("A player hits(" + (int)pJaw + ")"));
                Vector dv = relDir.crossProduct(new Vector(0, rotationSpeed, 0)).multiply(r/25); // retning x up||down = vel from hitting the bl8d
                Vector v = p.getVelocity();
                v.add(dv);
                p.setVelocity(v);
            } else {
                Bukkit.broadcast(Component.text("A player not(" + (int)pJaw + ")"));
            }
            if (r > 3.1) {
                players.remove(p);
                Bukkit.broadcast(Component.text("A player left"));
            }
        });
    }
}
