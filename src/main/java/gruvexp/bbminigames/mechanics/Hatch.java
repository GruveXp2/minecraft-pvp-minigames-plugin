package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.structure.Structure;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Hatch {

    private static final int TOTAL_STEPS = 20;

    private boolean open = false;
    private final HashSet<BlockDisplay> displays;
    private final HashSet<Block> openHitbox;
    private final HashSet<Block> closedHitbox;

    public Hatch(int id, Location location, StructureRotation rotation, String structureName) {
        this.displays = new HashSet<>();
        this.closedHitbox = new HashSet<>();
        this.openHitbox = new HashSet<>();

        Structure structure = BotBows.loadStructure(structureName);
        if (structure == null) return;

        Vector offset = structure.getSize().add(new Vector(-1, -1, -1));
        Vector openOffset = new Vector(offset.getBlockX(), offset.getBlockZ(), offset.getBlockY());
        Vector closedTarget = rotateVector(offset, rotation);
        Location originLoc = location.clone().add(-1, 0, 0);
        Vector origin = location.toVector().add(rotateVector(new Vector(-1, 0, 0), rotation));
        Vector openTarget = rotateVector(openOffset, rotation);

        Vector[] closedBounds = getBounds(origin, closedTarget);
        Vector[] openBounds = getBounds(origin, openTarget);

        Location hatchArea = new Location(Main.WORLD, origin.getX(), origin.getY(), origin.getZ());
        for (Entity nearbyEntity : hatchArea.getNearbyEntities(10, 10, 10)) {
            if (!(nearbyEntity instanceof BlockDisplay display)) continue;
            if (!display.getScoreboardTags().contains(structureName + "_" + id)) continue;

            displays.add(display);
            display.setRotation(display.getYaw(), 0);
        }

        if (displays.isEmpty()) {
            BotBows.placeSymmetricalStructure(structure, originLoc, location.clone().add(0.5, 0.5, 0.5), rotation, 2, structureName + "_" + id, displays);
        }

        for (int x = (int) closedBounds[0].getX(); x <= closedBounds[1].getX(); x++) {
            for (int y = (int) closedBounds[0].getY(); y <= closedBounds[1].getY(); y++) {
                for (int z = (int) closedBounds[0].getZ(); z <= closedBounds[1].getZ(); z++) {
                    Block block = Main.WORLD.getBlockAt(x, y, z);
                    block.setType(Material.BARRIER);
                    closedHitbox.add(block);
                }
            }
        }

        for (int x = (int) openBounds[0].getX(); x <= openBounds[1].getX(); x++) {
            for (int y = (int) openBounds[0].getY(); y <= openBounds[1].getY(); y++) {
                for (int z = (int) openBounds[0].getZ(); z <= openBounds[1].getZ(); z++) {
                    Block block = Main.WORLD.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                    openHitbox.add(block);
                }
            }
        }
    }

    private Vector[] getBounds(Vector origin, Vector size) {
        Vector end = origin.clone().add(size);

        Vector min = new Vector(
                Math.min(origin.getX(), end.getX()),
                Math.min(origin.getY(), end.getY()),
                Math.min(origin.getZ(), end.getZ())
        );

        Vector max = new Vector(
                Math.max(origin.getX(), end.getX()),
                Math.max(origin.getY(), end.getY()),
                Math.max(origin.getZ(), end.getZ())
        );

        return new Vector[]{min, max};
    }

    private Vector rotateVector(Vector vec, StructureRotation rotation) {
        double x = vec.getX();
        double z = vec.getZ();

        switch (rotation) {
            case CLOCKWISE_90 -> {
                vec.setX(-z);
                vec.setZ(x);
            }
            case COUNTERCLOCKWISE_90 -> {
                vec.setX(z);
                vec.setZ(-x);
            }
            case CLOCKWISE_180 -> {
                vec.setX(-x);
                vec.setZ(-z);
            }
            case NONE -> {
                // Nothing
            }
        }
        return vec;
    }

    public void toggle() {
        if (open) close();
        else open();
        open = !open;
    }

    public void open() {
        // shoot up players that stand on the hatch when it opens
        Set<Player> players = closedHitbox.iterator().next().getLocation().getNearbyEntities(4, 2, 3).stream()
                .filter(entity -> entity instanceof Player)
                .filter(entity -> entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BARRIER)
                .map(entity -> (Player) entity)
                .collect(Collectors.toSet());

        closedHitbox.forEach(block -> block.setType(Material.AIR));
        players.forEach(p -> {
            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                Vector v = p.getVelocity();
                v.add(new Vector(0, 1, 0));
                p.setVelocity(v);
            }
        });
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () ->
                openHitbox.forEach(block -> block.setType(Material.BARRIER)), TOTAL_STEPS / 2);
        // rotate them upwards
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Consumer<>() {
            final float jaw = displays.iterator().next().getYaw();
            float pitch = 0;
            @Override
            public void accept(BukkitTask task) {
                pitch -= 90f / TOTAL_STEPS;
                displays.forEach(display -> display.setRotation(jaw, pitch));
                if (pitch <= -90) {
                    task.cancel();
                    open = true;
                }
            }
        }, 0, 1);
    }

    public void close() {
        openHitbox.forEach(block -> block.setType(Material.AIR));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () ->
                closedHitbox.forEach(block -> block.setType(Material.BARRIER)), TOTAL_STEPS / 2);
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Consumer<>() {
            final float jaw = displays.iterator().next().getYaw();
            float pitch = -90;
            @Override
            public void accept(BukkitTask task) {
                pitch += 90f / TOTAL_STEPS;
                displays.forEach(display -> display.setRotation(jaw, pitch));
                if (pitch >= 0) {
                    task.cancel();
                    open = false;
                }
            }
        }, 0, 1);
    }
}
