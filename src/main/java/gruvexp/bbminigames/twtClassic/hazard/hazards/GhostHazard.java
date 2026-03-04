package gruvexp.bbminigames.twtClassic.hazard.hazards;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;

public class GhostHazard extends Hazard {

    private static final int HISTORY_SIZE = 100; // 5s * 20tick
    private static final ItemStack GHOST_SWORD = getGhostSword();
    private static final ItemStack GHOST_SWORD_NETHERITE = getGhostSwordNetherite();

    public GhostHazard(Lobby lobby) {
        super(lobby);
    }

    @Override
    public void init() {}

    @Override
    protected void trigger() {
        for (BotBowsPlayer bp : lobby.getPlayers()) {
            PlayerGhostMover ghostMover = new PlayerGhostMover(bp);
            ghostMover.runTaskTimer(Main.getPlugin(), 0L, 1L);
            hazardTimers.put(bp, ghostMover);

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                ghostMover.ascendGhost(bp.getLocation());

                float randomPitch = 0.8f + (float) Math.random() * 0.4f;
                Main.WORLD.playSound(lobby.settings.team1.spawnPos[0], "minecraft:botbows.ghost_rise", 1.0f, randomPitch);
                Main.WORLD.playSound(lobby.settings.team2.spawnPos[0], "minecraft:botbows.ghost_rise", 1.0f, randomPitch);
            }, 60L); // its 5 seconds delay, the ghost needs 2 seconds to ascend so it needs to ascend 3 seconds after starting to track the player
        }

        BotBows.setTimeSmooth(6000, 18000, 5);
    }

    @Override
    protected HazardMessage getAnnounceMessage() {
        return new HazardMessage("HAUNTED ARENA", "Stay in motion!", "HAUNTED ARENA");
    }

    @Override
    public String getName() {
        return "Haunted Arena";
    }

    @Override
    public Component[] getDescription() {
        return new Component[] {Component.text("When there is ghost mode, you will get haunted"),
                Component.text("by your own ghost, and when you touch it,"), Component.text("you die")};
    }

    @Override
    public String getActionDescription() {
        return "will be haunted by ghosts";
    }

    @Override
    public void end() {
        hazardTimers.values().forEach(t -> ((PlayerGhostMover) t).descendGhost());
        super.end();
        BotBows.setTimeSmooth(18000, 30000, 5);
    }

    private static ItemStack getGhostSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.SHARPNESS, 4);
        return sword;
    }

    private static ItemStack getGhostSwordNetherite() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        sword.addEnchantment(Enchantment.SHARPNESS, 4);
        return sword;
    }

    public static class PlayerGhostMover extends BukkitRunnable {

        final BotBowsPlayer bp;
        final ArrayDeque<Location> movementHistory = new ArrayDeque<>(HISTORY_SIZE);
        private boolean isClose = false;
        final ArmorStand ghost;
        public PlayerGhostMover(BotBowsPlayer bp) {
            this.bp = bp;
            ghost = spawnGhost();
        }

        @Override
        public void run() {
            Location pLoc = bp.getLocation();
            if (bp.isAlive() && bp.lobby.botBowsGame.canMove) {
                movementHistory.add(pLoc);
            }
            if (movementHistory.size() < HISTORY_SIZE && bp.isAlive()) return;
            if (movementHistory.isEmpty()) return;

            Location ghostLoc = movementHistory.poll();
            ghost.teleport(ghostLoc);
            if (BotBows.RANDOM.nextInt(5) == 0) { // randomly gjør at ghostene blinker for å gjøre det litt scary
                BotBowsAvatar.ArmorSet armor = bp.avatar.getArmor();
                ghost.getEquipment().setArmorContents(new ItemStack[]{armor.boots(), armor.leggings(), armor.chestplate(), armor.helmet()});
            } else {
                ghost.getEquipment().setArmorContents(new ItemStack[]{});
            }
            if (pLoc.distanceSquared(ghostLoc) < 36) {
                ghost.lookAt(pLoc, LookAnchor.EYES);
                if (!isClose) {
                    float randomPitch = 0.7f + (float) Math.random() * 0.2f;
                    bp.avatar.playSound(ghostLoc, "minecraft:botbows.ghost_approach", 1.0f, randomPitch);
                    isClose = true;
                }
                if (pLoc.distanceSquared(ghostLoc) < 9) {
                    ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD);
                    if (pLoc.distanceSquared(ghostLoc) < 1) {
                        killPlayer(bp);
                    }
                } else {
                    ghost.setItem(EquipmentSlot.HAND, null);
                }
            } else {
                isClose = false;
            }
        }

        private ArmorStand spawnGhost() {
            Location pLoc = bp.getLocation();
            ArmorStand ghost = pLoc.getWorld().spawn(pLoc, ArmorStand.class);
            ghost.setInvisible(true);
            ghost.setGravity(false);
            ghost.setMarker(true); // ingen hitbox
            ghost.setSilent(true);
            ghost.setArms(true);
            return ghost;
        }

        private void killPlayer(BotBowsPlayer bp) {
            cancel();
            movementHistory.clear();
            Location pLoc = bp.getLocation();
            Vector dir = pLoc.getDirection().multiply(-1).setY(0).normalize();

            Location ghostLoc = pLoc.clone().add(dir); // 0.5 blocks behind the player
            ghostLoc.add(dir.crossProduct(new Vector(0, 1, 0)).normalize().multiply(0.5)); // 0.5 blocks left of the player
            ghost.teleport(ghostLoc);

            ghost.setRotation(pLoc.getYaw(), pLoc.getPitch());
            ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD_NETHERITE);
            bp.avatar.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));

            new BukkitRunnable() {
                final Location oldLocation = pLoc;
                int ticks = 0;
                final BotBowsGame game = bp.lobby.botBowsGame;
                @Override
                public void run() {
                    if (game == null) { // if the game ended before the animation completed, then stop
                        cancel();
                        descendGhost(ghostLoc);
                        return;
                    }
                    if (!bp.isAlive() || ticks >= 40) {
                        cancel();
                        bp.die(bp.getName()
                                .append(Component.text(" was ghosted", NamedTextColor.DARK_GRAY)));
                        descendGhost(ghostLoc);
                        return;
                    }
                    oldLocation.setYaw(bp.getLocation().getYaw());
                    oldLocation.setPitch(bp.getLocation().getPitch());

                    bp.teleport(oldLocation);
                    ticks++;
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        public void descendGhost() {
            descendGhost(ghost.getLocation());
        }

        private void descendGhost(Location ghostLoc) {
            cancel();
            final int blocks = -2;
            final int totalTicks = 2 * 20;
            final double Δy = (double) blocks / totalTicks;
            new BukkitRunnable() { // slowly moves the ghost into the ground
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= totalTicks) {
                        cancel();
                        ghost.remove(); // the ghost is in the ground and can be removed
                        return;
                    }
                    ghostLoc.add(0, Δy, 0);
                    ghost.teleport(ghostLoc);
                    ticks++;
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        public void ascendGhost(Location pLoc) {
            final int blocks = 2;
            final int totalTicks = 2 * 20;
            final double Δy = (double) blocks / totalTicks;
            ghost.teleport(pLoc.subtract(0, blocks, 0));
            new BukkitRunnable() { // slowly moves the ghost out of the ground
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks >= totalTicks) {
                        cancel();
                        return;
                    }
                    pLoc.add(0, Δy, 0);
                    ghost.teleport(pLoc);
                    ticks++;
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }
    }
}
