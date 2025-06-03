package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitRunnable;

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
}
