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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class StormHazard extends Hazard {

    public StormHazard(Lobby lobby) {
        super(lobby);
    }

    public void init() { // calles når spillet begynner
        if (getChance() == HazardChance.DISABLED) return;
        for (BotBowsPlayer bp : lobby.getPlayers()) {
            BossBar bar = BossBar.bossBar(Component.text("Lightning timer", NamedTextColor.AQUA), 0, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_6);
            bp.avatar.initHazardBar(HazardType.STORM, bar);
        }
    }

    @Override
    protected void trigger() {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (BotBowsPlayer bp : lobby.getPlayers()) {
                PlayerStormTimer stormTimer = new PlayerStormTimer(bp);
                stormTimer.runTaskTimer(Main.getPlugin(), 0L, 2L);
                hazardTimers.put(bp, stormTimer);
            }
            Main.WORLD.setThundering(true);
            Main.WORLD.setStorm(true);
            Main.WORLD.setThunderDuration(12000); //10min
        }, 100L); // 5 sekunder
    }

    @Override
    protected HazardMessage getAnnounceMessage() {
        return new HazardMessage("STORM INCOMING!", "Seek shelter immediately!", "STORM INCOMING");
    }

    @Override
    public String getName() {
        return "Storms";
    }

    @Override
    public Component[] getDescription() {
        return new Component[] {Component.text("When there is a storm, you will get hit by"),
                Component.text("lightning if you stand in dirext exposure"),
                Component.text("to the sky for more than 5 seconds")};
    }

    @Override
    public String getActionDescription() {
        return "will have storms";
    }

    @Override
    public void end() {
        super.end();
        // resett været
        Main.WORLD.setThundering(false);
        Main.WORLD.setStorm(false);
        Main.WORLD.setClearWeatherDuration(12000);
    }

    public static class PlayerStormTimer extends BukkitRunnable {

        static final int GROUND_LEVEL = 22;
        static final int UPPER_BOUND = 29;
        static final int SECONDS = 6; // hvor lenge man kan stå før man blir tatt av lightning

        final BotBowsPlayer bp;
        int time = 0;
        public PlayerStormTimer(BotBowsPlayer bp) {
            this.bp = bp;
        }

        private boolean isPlayerExposed() {
            Location pLoc = bp.getLocation();
            if (pLoc.getY() < GROUND_LEVEL) {return false;}
            if (pLoc.getY() >= UPPER_BOUND) {return true;}

            int x = pLoc.getBlockX();
            int y = pLoc.getBlockY();
            int z = pLoc.getBlockZ();
            //p.sendMessage(ChatColor.GRAY + "======== [DEBUG] ========\np_coord = " + p.getLocation().getX() + ", " + p.getLocation().getY() + ", " + p.getLocation().getZ());
            for (int i = y + 2; i < UPPER_BOUND + 2; i++) {
                //p.sendMessage(ChatColor.GRAY + "" + x + ", " + y + ", " + z + " : " + world.getBlockAt(x, y, z).getType());
                if (Main.WORLD.getBlockAt(x, i, z).getType() != Material.AIR) {return false;}
            }
            return true;
        }

        @Override
        public void run() { // annehver tick = 10Hz
            if (!bp.isAlive()) return; // if the player is dead, dont do anything
            if (isPlayerExposed()) {
                if (time < SECONDS*40) { // 40 = run().frekvens*hvor_mye
                    time += 4; // tida går opp 4x så kjapt som når cooldownen går ned. Altså går tida opp 1s/s
                    if (time >= SECONDS*40) {
                        bp.avatar.setHazardBarProgress(HazardType.STORM, 1);
                    } else {
                        bp.avatar.setHazardBarProgress(HazardType.STORM, (float) time /(SECONDS*40));
                    }
                } else {
                    Main.WORLD.strikeLightningEffect(bp.getLocation());
                    time = 0; // resetter
                    bp.avatar.setHazardBarProgress(HazardType.STORM, 0);
                    bp.die(bp.getName()
                            .append(Component.text(" was electrocuted to a crisp!", NamedTextColor.AQUA)));
                }
            } else {
                if (time > 0) {
                    time--; // cooldownen går ned 0.25s/s
                    bp.avatar.setHazardBarProgress(HazardType.STORM, (float) time /(SECONDS*40));
                }
            }
        }
    }
}
