package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
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

    public Hatch(String displayTag, Vector closedHitboxOrigin, Vector closedHitboxSize, Vector openHitboxOrigin, Vector openHitboxSize) {
        this.displays = new HashSet<>();
        this.closedHitbox = new HashSet<>();
        this.openHitbox = new HashSet<>();
        Location hatchArea = new Location(Main.WORLD, openHitboxOrigin.getX(), openHitboxOrigin.getY(), openHitboxOrigin.getZ());
        for (Entity nearbyEntity : hatchArea.getNearbyEntities(10, 10, 10)) {
            if (!(nearbyEntity instanceof BlockDisplay display)) continue;
            if (!display.getScoreboardTags().contains(displayTag)) continue;

            displays.add(display);
            display.setRotation(display.getYaw(), 0);
        }
        for (int x = (int) closedHitboxOrigin.getX(); x < closedHitboxOrigin.getX() + closedHitboxSize.getX(); x++) {
            for (int y = (int) closedHitboxOrigin.getY(); y < closedHitboxOrigin.getY() + closedHitboxSize.getY(); y++) {
                for (int z = (int) closedHitboxOrigin.getZ(); z < closedHitboxOrigin.getZ() + closedHitboxSize.getZ(); z++) {
                    Block block = Main.WORLD.getBlockAt(x, y, z);
                    block.setType(Material.BARRIER);
                    closedHitbox.add(block);
                }
            }
        }
        for (int x = (int) openHitboxOrigin.getX(); x < openHitboxOrigin.getX() + openHitboxSize.getX(); x++) {
            for (int y = (int) openHitboxOrigin.getY(); y < openHitboxOrigin.getY() + openHitboxSize.getY(); y++) {
                for (int z = (int) openHitboxOrigin.getZ(); z < openHitboxOrigin.getZ() + openHitboxSize.getZ(); z++) {
                    Block block = Main.WORLD.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                    openHitbox.add(block);
                }
            }
        }
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
