package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.*;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;

import java.util.HashSet;
import java.util.Set;

public class Gear {

    private final Set<BlockDisplay> displays;
    private final float rotationStep;
    private final float jaw;

    private float pitch = 0f;
    public final String tag;

    public Gear(Set<BlockDisplay> displays, float rotationStep, String tag) {
        this.displays = displays;
        this.rotationStep = rotationStep;
        this.jaw = displays.iterator().next().getYaw();
        this.tag = tag;
    }

    public Gear(GearConfig config) {
        displays = new HashSet<>();
        rotationStep = config.speed;
        jaw = config.rotation == StructureRotation.CLOCKWISE_90 || config.rotation == StructureRotation.COUNTERCLOCKWISE_90 ? 90 : 0;
        tag = config.structureName;
        Structure structure = BotBows.loadStructure(config.structureName);
        if (structure == null) return;
        Location location = config.location;
        StructureRotation rotation = config.rotation;
        BotBows.placeSymmetricalStructure(structure, location.clone().add(0, -2, -2), location.clone().add(0, 0.5, 0.5), rotation, 1, tag + "_" + config.id(), displays);
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

        new BukkitRunnable() {
            float degreesLeft = degrees;

            @Override
            public void run() {
                if (degreesLeft <= 0) cancel();

                rotate();
                degreesLeft -= stepDegrees;
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    private void rotateTo(float newPitch) {
        displays.forEach(display -> display.setRotation(jaw, newPitch));
        pitch = newPitch;
    }

    public float getRotationSpeed() {
        return Math.abs(rotationStep);
    }

    public record GearConfig(int id, Location location, StructureRotation rotation, String structureName, float speed) {}
}
