package gruvexp.bbminigames.twtClassic.hazard.hazards;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class EarthquakeHazard extends Hazard {
    Set<Location> anvilLocations = new HashSet<>();

    public EarthquakeHazard(Lobby lobby) {
        super(lobby);
    }

    public void init() { // calles når spillet begynner
        if (getChance() == HazardChance.DISABLED) return;
        for (BotBowsPlayer bp : lobby.getPlayers()) {
            BossBar bar = BossBar.bossBar(Component.text("Anvil timer", NamedTextColor.GOLD), 0, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_6);
            bp.avatar.initHazardBar(HazardType.EARTHQUAKE, bar);
        }
    }
    @Override
    protected void trigger() {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (BotBowsPlayer bp : lobby.getPlayers()) {
                PlayerEarthQuakeTimer earthQuakeTimer = new PlayerEarthQuakeTimer(bp);
                earthQuakeTimer.runTaskTimer(Main.getPlugin(), 0L, 2L);
                hazardTimers.put(bp, earthQuakeTimer);
            }
        }, 100L); // 5 sekunder
    }

    @Override
    protected HazardMessage getAnnounceMessage() {
        return new HazardMessage("EARTHQUAKE INCOMING!", "Stay above ground!", "EARTHQUAKE INCOMING");
    }

    @Override
    public String getName() {
        return "Earthquakes";
    }

    @Override
    public Component[] getDescription() {
        return new Component[] {Component.text("When there is an earthwuake, you will get hit by"),
                Component.text("stones if you go underground"),
                Component.text("for more than 5 seconds")};
    }

    @Override
    public String getActionDescription() {
        return "will have storms";
    }

    @Override
    public void end() {
        super.end();
        for (Location anvilLocation : anvilLocations) {
            Block block = anvilLocation.getBlock();
            if (block.getType() == Material.ANVIL) {
                anvilLocation.getBlock().setType(Material.AIR);
            }
        }
        anvilLocations.clear();
    }

    public class PlayerEarthQuakeTimer extends BukkitRunnable {

        static final int GROUND_LEVEL = 22;
        static final int UPPER_BOUND = 29;
        static final int SECONDS = 6; // hvor lenge man kan stå før Einstein kommer p

        final BotBowsPlayer bp;
        int time = 0;
        public PlayerEarthQuakeTimer(BotBowsPlayer bp) {
            this.bp = bp;
        }

        private boolean isPlayerUnderground() {
            Location loc = bp.getLocation();
            if (loc.getY() >= GROUND_LEVEL) {return false;}

            int x = (int) Math.floor(loc.getX());
            int y = (int) Math.floor(loc.getY());
            int z = (int) Math.floor(loc.getZ());
            //p.sendMessage(ChatColor.GRAY + "======== [DEBUG] ========\np_coord = " + p.getLocation().getX() + ", " + p.getLocation().getY() + ", " + p.getLocation().getZ());
            for (int i = y + 2; i < UPPER_BOUND + 2; i++) {
                //p.sendMessage(ChatColor.GRAY + "" + x + ", " + y + ", " + z + " : " + world.getBlockAt(x, y, z).getType());
                if (Main.WORLD.getBlockAt(x, i, z).getType() != Material.AIR) {return true;}
            }
            return false;
        }

        @Override
        public void run() { // annehver tick = 10Hz
            if (!bp.isAlive()) return; // if the player is dead, dont do anything
            if (isPlayerUnderground()) {
                if (time < SECONDS*40) { // 40 = run().frekvens*hvor_mye
                    time += 4; // tida går opp 4x så kjapt som når cooldownen går ned. Altså går tida opp 1s/s
                    if (time >= SECONDS*40) {
                        bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, 1);
                    } else {
                        bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, (float) time /(SECONDS*40));
                    }
                } else {
                    FallingBlock fallingAnvil = Main.WORLD.spawnFallingBlock(bp.getLocation().add(0, 3.9, 0), Material.ANVIL.createBlockData());
                    fallingAnvil.setHurtEntities(true);
                    fallingAnvil.setDropItem(false);
                    time = 0; // resetter
                    bp.avatar.setHazardBarProgress(HazardType.STORM, 0);
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> bp.die(bp.getName()
                            .append(Component.text(" was squashed by a small stone the size of a large boulder", NamedTextColor.GOLD))), 20L);
                    Location anvilLoc = bp.getLocation().toBlockLocation();
                    while (anvilLoc.getBlock().getType() == Material.AIR) {
                        anvilLoc.subtract(0, 1, 0);
                    }
                    anvilLoc.add(0, 1, 0);
                    anvilLocations.add(anvilLoc);
                }
            } else {
                if (time > 0) {
                    time--; // cooldownen går ned 0.25s/s
                    bp.avatar.setHazardBarProgress(HazardType.EARTHQUAKE, (float) time /(SECONDS*40));
                }
            }
        }
    }
}
