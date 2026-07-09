package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Rotor {
    private final Set<BlockDisplay> displays;
    private final float rotationStep;
    private BukkitRunnable runnable;

    private float jaw = 0f;
    public final String tag;

    public Rotor(int id, Location location, String structureName, float speed, int teleportDuration) {
        displays = new HashSet<>();
        rotationStep = speed * (BotBows.RANDOM.nextInt(2) == 1 ? 1 : -1) * (1 + BotBows.RANDOM.nextInt(4)) / 25f;
        tag = structureName;

        for (Entity nearbyEntity : location.getNearbyEntities(2, 2, 2)) {
            if (!(nearbyEntity instanceof BlockDisplay display)) continue;
            if (!display.getScoreboardTags().contains(structureName + "_" + id)) continue;

            displays.add(display);
            display.setRotation(display.getYaw(), 0);
        }
        if (displays.isEmpty()) {
            Structure structure = BotBows.loadStructure(structureName);
            if (structure == null) return;
            Vector size = structure.getSize().multiply(0.5);
            BotBows.placeSymmetricalStructure(structure, location.clone().add(-size.getBlockX(), -size.getBlockY(), -size.getBlockZ()), location.clone().add(0.5, 0.5, 0.5), StructureRotation.NONE, teleportDuration, tag + "_" + id, displays);
        }
    }

    public void startRotating() {
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                jaw += rotationStep;
                displays.forEach(display -> display.setRotation(jaw, 0));
            }
        };
        runnable.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public void stop() {
        runnable.cancel();
    }
}
