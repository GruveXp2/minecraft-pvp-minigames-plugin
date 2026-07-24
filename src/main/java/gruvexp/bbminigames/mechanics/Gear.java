package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.*;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Gear {

    private final Set<BlockDisplay> displays;
    private final float rotationStep;
    private final float jaw;
    private BukkitRunnable runnable;

    private float pitch = 0f;
    public final String tag;

    public Gear(int id, Location location, StructureRotation rotation, String structureName, float speed) {
        this(id, location, rotation, structureName, speed, 1);
    }

    public Gear(int id, Location location, StructureRotation rotation, String structureName, float speed, int teleportDuration) {
        displays = new HashSet<>();
        rotationStep = speed;
        jaw = rotation == StructureRotation.CLOCKWISE_90 || rotation == StructureRotation.COUNTERCLOCKWISE_90 ? 90 : 0;
        tag = structureName;

        for (Entity nearbyEntity : location.getNearbyEntities(10, 10, 10)) {
            if (!(nearbyEntity instanceof BlockDisplay display)) continue;
            if (!display.getScoreboardTags().contains(structureName + "_" + id)) continue;

            displays.add(display);
            display.setRotation(display.getYaw(), 0);
        }
        if (displays.isEmpty()) {
            Structure structure = BotBows.loadStructure(structureName);
            if (structure == null) return;
            Vector size = structure.getSize().multiply(0.5);
            BotBows.placeSymmetricalStructure(structure, location.clone().add(-size.getBlockX(), -size.getBlockY(), -size.getBlockZ()), location.clone().add(0, 0.5, 0.5), rotation, teleportDuration, tag + "_" + id, displays);
        }
    }

    private void rotate() {
        pitch += rotationStep;
        if (pitch > 91) {
            rotateTo(-90);
        } else if (pitch < -91) {
            rotateTo(90);
        }
        displays.forEach(display -> display.setRotation(jaw, pitch));
    }

    public void rotate(float degrees) {
        float stepDegrees = Math.abs(rotationStep);

        runnable = new BukkitRunnable() {
            float degreesLeft = degrees;

            @Override
            public void run() {
                if (degreesLeft <= 0) cancel();

                rotate();
                degreesLeft -= stepDegrees;
            }
        };
        runnable.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    private void rotateTo(float newPitch) {
        displays.forEach(display -> display.setRotation(jaw, newPitch));
        pitch = newPitch;
    }

    public void stop() {
        runnable.cancel();
    }

    public float getRotationSpeed() {
        return Math.abs(rotationStep);
    }
}
