package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

import java.util.HashSet;

public class SpaceStationDoor {

    private static final int ANIMATION_STEP_TICKS = 2;
    private static final Location STRUC_SRC_X = new Location(Main.WORLD_END, 139, 2, 195);
    private static final Location STRUC_SRC_Z = new Location(Main.WORLD_END, 129, 2, 205);
    private static final Vector3i SIZE_X = new Vector3i(1, 7, 5);
    private static final Vector3i SIZE_Z = new Vector3i(5, 7, 1);
    private static final int ANIMATION_STEPS = 4;

    private final HashSet<Block> shulkerBoxes = new HashSet<>();

    private final Location location; // where the door is in the map
    private final Axis axis;

    private boolean open;

    public SpaceStationDoor(Location location, Axis axis) {
        this.location = location;
        this.axis = axis;
        if (axis == Axis.X) {
            shulkerBoxes.add(location.clone().add( 1, 3, 0).getBlock());
            shulkerBoxes.add(location.clone().add(-1, 3, 0).getBlock());
            shulkerBoxes.add(location.clone().add( 1, 3, 4).getBlock());
            shulkerBoxes.add(location.clone().add(-1, 3, 4).getBlock());
        } else {
            shulkerBoxes.add(location.clone().add(0, 3,  1).getBlock());
            shulkerBoxes.add(location.clone().add(0, 3, -1).getBlock());
            shulkerBoxes.add(location.clone().add(4, 3,  1).getBlock());
            shulkerBoxes.add(location.clone().add(4, 3, -1).getBlock());
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        open = !open;
        setShulkerBoxes();
        setOutline();
        if (open) close();
        else open();
    }

    public void open() {
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                cloneBlocks(step);
                step++;
                if (step == ANIMATION_STEPS) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 0, ANIMATION_STEP_TICKS);
    }

    public void close() {
        new BukkitRunnable() {
            int step = ANIMATION_STEPS - 1;
            @Override
            public void run() {
                cloneBlocks(step);
                step--;
                if (step < 0) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 0, ANIMATION_STEP_TICKS);
    }

    private void cloneBlocks(int step) {
        int offset = open ? 5 : 0;
        Location stepOrigin = axis == Axis.X ? STRUC_SRC_X.clone().add(step, 0, offset) : STRUC_SRC_Z.clone().add(offset, 0, step);
        Vector3i size = axis == Axis.X ? SIZE_X : SIZE_Z;
        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z < size.z; z++) {
                    BlockData data = Main.WORLD_END.getBlockData(stepOrigin.getBlockX() + x, stepOrigin.getBlockY() + y, stepOrigin.getBlockZ() + z);
                    location.getWorld().setBlockData(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z, data);
                }
            }
        }
    }

    public void setShulkerBoxes() {
        Material material = open ? Material.LIME_SHULKER_BOX : Material.RED_SHULKER_BOX;
        shulkerBoxes.forEach(block -> {
            Directional oldRotation = (Directional) block.getBlockData();
            block.setType(material);
            Directional newRotation = (Directional) block.getBlockData();
            newRotation.setFacing(oldRotation.getFacing());
            block.setBlockData(newRotation);
        });
    }

    public void setOutline() {
        Vector3i size = axis == Axis.X ? SIZE_X : SIZE_Z;
        for (int x = -2; x < size.x + 2; x++) {
            for (int y = -1; y < size.y + 1; y++) {
                for (int z = -2; z < size.z + 2; z++) {
                    Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
                    Material material = block.getType();
                    if (open && material == Material.RED_CONCRETE) {
                        block.setType(Material.LIME_CONCRETE);
                    } else if (!open && material == Material.LIME_CONCRETE) {
                        block.setType(Material.RED_CONCRETE);
                    }
                }
            }
        }
    }
}
