package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Wall;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.List;

public class LaserTrap extends Ability {

    private LaserEmitter emitter;

    public LaserTrap(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.LASER_TRAP);
    }

    public void use(Block block, BlockFace face) {
        if (emitter != null) emitter.remove();
        emitter = new LaserEmitter(block, face);
        emitter.runTaskTimer(Main.getPlugin(), 0, 1);
        use();
    }

    @Override
    public void unequip() {
        reset();
    }

    @Override
    public void reset() {
        if (emitter != null) {
            emitter.remove();
        }
    }

    public class LaserEmitter extends BukkitRunnable {

        private final Location origin;
        private final Location center;
        private final Location end;
        private final Vector offset;
        private final Vector laserUnitOffset;
        private final int length;
        private final Color color;
        private final World world;
        private final List<BotBowsPlayer> opponents;

        public LaserEmitter(Block block, BlockFace face) {

            origin = block.getLocation().add(0.5, 0.5, 0.5);
            laserUnitOffset = face.getDirection();
            Vector mid1 = new Vector(0.5, 0.5, 0.5).add(face.getDirection().multiply(0.1)); // midtpunkt i blokken
            Vector mid2 = mid1.add(face.getDirection().multiply(-0.2)); // midtpunkt i blokken
            int length = 0;

            for (int i = 0; i < 100; i++) {
                block = block.getRelative(face);

                if (block.getType() == Material.AIR || !block.isSolid() || block.getType().isTransparent()) {
                    length++;
                    continue;
                }
                BlockData blockData = block.getBlockData();
                if (block.getType().isOccluding()) break;

                if (blockData instanceof Stairs
                        || blockData instanceof Slab
                        || blockData instanceof Wall) {
                    VoxelShape shape = block.getBlockData().getCollisionShape(block.getLocation());
                    if (!shape.getBoundingBoxes().isEmpty()) {
                        boolean hit = shape.getBoundingBoxes().stream()
                                .anyMatch(b -> b.contains(mid1) || b.contains(mid2));
                        if (hit) break;
                    }
                }
                length++;
            }
            end = block.getLocation().add(0.5, 0.5, 0.5).subtract(face.getDirection().multiply(0.6));
            offset = face.getDirection().multiply(- length/2);
            center = block.getLocation().add(offset).add(0.5, 0.5, 0.5);
            offset.setX(Math.abs(offset.getX())).setY(Math.abs(offset.getY())).setZ(Math.abs(offset.getZ())); // Its just abs() for every x y z

            this.length = length;
            this.color = bp.getTeam().dyeColor.getColor();
            this.opponents = bp.getTeam().getOppositeTeam().getPlayers();
            this.world = center.getWorld();
        }

        @Override
        public void run() {
            for (BotBowsPlayer defender : opponents) {
                Location proximity = defender.player.getLocation().add(0, 1, 0).subtract(center);
                if (Math.abs(proximity.getX()) < offset.getX() + 0.5 &&
                        Math.abs(proximity.getY()) < offset.getY() + 1 &&
                        Math.abs(proximity.getZ()) < offset.getZ() + 0.5) {
                    defender.handleHit(Component.text(" got enlightened by "), bp, Component.text("'s laser beam"));
                }
            }
            Location loc = origin.clone();
            loc.add(laserUnitOffset.clone().multiply(0.5));
            for (int i = 0; i < length; i++) {
                world.spawnParticle(Particle.DUST, loc, 1, laserUnitOffset.getX(), laserUnitOffset.getY(), laserUnitOffset.getZ(), 0.1, new Particle.DustOptions(color, 1), true);
                world.spawnParticle(Particle.DUST, loc, 4, laserUnitOffset.getX(), laserUnitOffset.getY(), laserUnitOffset.getZ(), 0.1, new Particle.DustOptions(Color.RED, 0.2f), true);
                loc.add(laserUnitOffset);
            }
            world.spawnParticle(Particle.DUST, end, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1.5f), true);
        }

        public void remove() {
            cancel();
            origin.getBlock().setType(Material.AIR);
        }
    }
}
