package gruvexp.bbminigames.twtClassic.hazard.hazards;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class StormHazard extends Hazard {

    final HashMap<BotBowsPlayer, BossBar> bars = new HashMap<>(3);

    public StormHazard(Lobby lobby) {
        super(lobby);
    }

    public void init() { // calles når spillet begynner
        if (getChance() == HazardChance.DISABLED) return;
        for (BotBowsPlayer bp : lobby.getPlayers()) {
            BossBar bar = Bukkit.createBossBar(ChatColor.AQUA + "Lightning timer", BarColor.BLUE, BarStyle.SEGMENTED_6);
            bar.addPlayer(bp.player);
            bar.setProgress(0d);
            bar.setVisible(false);
            bars.put(bp, bar);
        }
    }

    @Override
    protected void trigger() {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (BotBowsPlayer bp : lobby.getPlayers()) {
                PlayerStormTimer stormTimer = new PlayerStormTimer(bp, bars.get(bp));
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
        for (BossBar bossBar : bars.values()) { // resett storm baren og skjul den
            bossBar.setVisible(false);
            bossBar.setProgress(0d);
        }
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
        final BossBar bar;
        int time = 0;
        public PlayerStormTimer(BotBowsPlayer bp, BossBar bar) {
            this.bp = bp;
            this.bar = bar;
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
                        bar.setProgress(1.0d);
                    } else {
                        bar.setProgress(time/(SECONDS*40d));
                    }
                    if (time >= 4) { // baren vises bare når det er nødvendig, hvis den er 0 så er man i sikkerhet, men om den er over 0 betyr det enten at man er i fare eller så kan man se hvor lenge er igjen av timeren
                        bar.setVisible(true);
                    }
                } else {
                    Main.WORLD.strikeLightningEffect(bp.getLocation());
                    time = 0; // resetter
                    bar.setProgress(0);
                    bp.die(bp.getName()
                            .append(Component.text(" was electrocuted to a crisp!", NamedTextColor.AQUA)));
                }
            } else {
                if (time > 0) {
                    time--; // cooldownen går ned 0.25s/s
                    bar.setProgress(time/(SECONDS*40d));
                    if (time == 0) {
                        bar.setVisible(false);
                    }
                }
            }
        }
    }
}
