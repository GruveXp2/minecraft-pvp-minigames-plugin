package gruvexp.bbminigames.twtClassic.hazard.hazards;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
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
    protected void trigger() {
        lobby.messagePlayers(Component.text("HAUNTED ARENA", NamedTextColor.DARK_RED)
                .append(Component.text(" Stay in motion!", NamedTextColor.RED)));
        lobby.titlePlayers(ChatColor.RED + "HAUNTED ARENA", 80);

        for (BotBowsPlayer p : lobby.getPlayers()) {
            PlayerGhostMover ghostMover = new PlayerGhostMover(p);
            ghostMover.runTaskTimer(Main.getPlugin(), 0L, 1L);
            hazardTimers.put(p.player, ghostMover);

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                ghostMover.ascendGhost(p.player.getLocation());

                float randomPitch = 0.8f + (float) Math.random() * 0.4f;
                Main.WORLD.playSound(lobby.settings.team1.spawnPos[0], "minecraft:botbows.ghost_rise", 1.0f, randomPitch);
                Main.WORLD.playSound(lobby.settings.team2.spawnPos[0], "minecraft:botbows.ghost_rise", 1.0f, randomPitch);
            }, 60L); // its 5 seconds delay, the ghost needs 2 seconds to ascend so it needs to ascend 3 seconds after starting to track the player
        }

        BotBows.setTimeSmooth(6000, 18000, 5);
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

        final Player p;
        final BotBowsPlayer bp;
        final ArrayDeque<Location> movementHistory = new ArrayDeque<>(HISTORY_SIZE);
        private boolean isClose = false;
        final ArmorStand ghost;
        public PlayerGhostMover(BotBowsPlayer bp) {
            this.p = bp.player;
            this.bp = bp;
            ghost = spawnGhost();
        }

        @Override
        public void run() {
            if (bp.isAlive() && bp.lobby.botBowsGame.canMove) {
                movementHistory.add(p.getLocation());
            }
            if (movementHistory.size() < HISTORY_SIZE && bp.isAlive()) return;
            if (movementHistory.isEmpty()) return;

            Location ghostLoc = movementHistory.poll();
            ghost.teleport(ghostLoc);
            if (BotBows.RANDOM.nextInt(5) == 0) { // randomly gjør at ghostene blinker for å gjøre det litt scary
                ItemStack[] armor = p.getInventory().getArmorContents();
                ghost.getEquipment().setArmorContents(armor);
            } else {
                ghost.getEquipment().setArmorContents(new ItemStack[]{});
            }
            if (p.getLocation().distanceSquared(ghost.getLocation()) < 36) {
                ghost.lookAt(p.getLocation(), LookAnchor.EYES);
                if (!isClose) {
                    float randomPitch = 0.7f + (float) Math.random() * 0.2f;
                    p.playSound(ghostLoc, "minecraft:botbows.ghost_approach", 1.0f, randomPitch);
                    isClose = true;
                }
                if (p.getLocation().distanceSquared(ghost.getLocation()) < 9) {
                    ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD);
                    if (p.getLocation().distanceSquared(ghost.getLocation()) < 1) {
                        if (TestCommand.test2 && p.getName().equals("Spionagent54")) return;
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
            ArmorStand ghost = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
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
            Player p = bp.player;
            Location playerLoc = p.getLocation();
            Vector dir = playerLoc.getDirection().multiply(-1).setY(0).normalize();

            Location ghostLoc = playerLoc.clone().add(dir); // 0.5 blocks behind the player
            ghostLoc.add(dir.crossProduct(new Vector(0, 1, 0)).normalize().multiply(0.5)); // 0.5 blocks left of the player
            ghost.teleport(ghostLoc);

            ghost.setRotation(playerLoc.getYaw(), playerLoc.getPitch());
            ghost.setItem(EquipmentSlot.HAND, GHOST_SWORD_NETHERITE);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));

            new BukkitRunnable() {
                final Location oldLocation = p.getLocation();
                int ticks = 0;
                @Override
                public void run() {
                    if (!p.isOnline() || ticks >= 40) {
                        cancel();
                        bp.die(Component.text(p.getName(), bp.getTeamColor())
                                .append(Component.text(" was ghosted", NamedTextColor.DARK_GRAY)));
                        descendGhost(ghostLoc);
                        return;
                    }
                    oldLocation.setYaw(p.getLocation().getYaw());
                    oldLocation.setPitch(p.getLocation().getPitch());

                    p.teleport(oldLocation);
                    ticks++;
                }
            }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        private void descendGhost(Location ghostLoc) {
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
