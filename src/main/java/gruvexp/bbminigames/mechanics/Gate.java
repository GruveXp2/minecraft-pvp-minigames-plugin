package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.botbowsGames.SteamPunkGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Gate {

    private static final int spacing = 1;
    private static final int ROTATION_ANGLE = 360;

    private final Location structureSrc; // where the door parts are located
    private final int animationSteps;
    private final Vector3i size;
    private final Location location; // where the door is in the map
    private final int animationStepTicks;
    private final Set<Gear> gears = new HashSet<>();

    private boolean open;

    public Gate(Location structureSrc, int animationSteps, Vector3i size, Location location, int animationStepTicks, boolean startsOpen, Map<String, Float> gears) {
        this.structureSrc = structureSrc;
        this.animationSteps = animationSteps;
        this.size = size;
        this.location = location;
        this.animationStepTicks = animationStepTicks;
        this.open = startsOpen;

        gears.forEach((tag, speed) -> {
            Set<BlockDisplay> displays = new HashSet<>();
            for (Entity nearbyEntity : location.getNearbyEntities(20, 10, 10)) {
                if (!(nearbyEntity instanceof BlockDisplay display)) continue;
                if (!display.getScoreboardTags().contains(tag)) continue;

                displays.add(display);
                display.setRotation(display.getYaw(), 0);
            }
            this.gears.add(new Gear(displays, speed, tag));
        });
    }

    public void toggle() {
        if (open) close();
        else open();
        open = !open;
    }

    public void open() {
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                cloneBlocks(step);
                step++;
                if (step == animationSteps) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 0, animationStepTicks);
        int delay = (int) (SteamPunkGame.DOOR_TOGGLE_DELAY - ROTATION_ANGLE / gears.iterator().next().getRotationSpeed());
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> gears.forEach(gear -> gear.rotate(ROTATION_ANGLE)), delay);
    }

    public void close() {
        new BukkitRunnable() {
            int step = animationSteps - 1;
            @Override
            public void run() {
                cloneBlocks(step);
                step--;
                if (step < 0) cancel();
            }


        }.runTaskTimer(Main.getPlugin(), 0, animationStepTicks);
    }

    private void cloneBlocks(int step) {
        Location stepOrigin = structureSrc.clone().add(step * (size.x + spacing), 0, 0);
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    BlockData data = Main.WORLD.getBlockData(stepOrigin.getBlockX() + x, stepOrigin.getBlockY() + y, stepOrigin.getBlockZ() + z);
                    location.getWorld().setBlockData(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z, data);
                }
            }
        }
    }
}
