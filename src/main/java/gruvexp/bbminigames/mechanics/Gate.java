package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

public class Gate {

    private static final int spacing = 1;

    private final Location origin; // where the door parts are located
    private final int animationSteps;
    private final Vector3i size;
    private final Location location; // where the door is in the map
    private final int animationStepTicks;

    private boolean open;

    public Gate(Location origin, int animationSteps, Vector3i size, Location location, int animationStepTicks, boolean startsOpen) {
        this.origin = origin;
        this.animationSteps = animationSteps;
        this.size = size;
        this.location = location;
        this.animationStepTicks = animationStepTicks;
        this.open = startsOpen;
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
        Location stepOrigin = origin.clone().add(step * (size.x + spacing), 0, 0);
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
