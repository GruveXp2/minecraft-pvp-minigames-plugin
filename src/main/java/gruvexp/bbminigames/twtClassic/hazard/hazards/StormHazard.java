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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class StormHazard extends Hazard {

    final HashMap<BotBowsPlayer, BossBar> bars = new HashMap<>(3);

    public StormHazard(Lobby lobby) {
        super(lobby);
    }

    public void init() { // calles når spillet begynner
        if (getChance() == HazardChance.DISABLED) return;
        for (BotBowsPlayer p : lobby.getPlayers()) {
            BossBar bar = Bukkit.createBossBar(ChatColor.AQUA + "Lightning timer", BarColor.BLUE, BarStyle.SEGMENTED_6);
            bar.addPlayer(p.player);
            bar.setProgress(0d);
            bar.setVisible(false);
            bars.put(p, bar);
        }
    }

    @Override
    protected void trigger() {
        lobby.messagePlayers(Component.text("STORM INCOMING!", NamedTextColor.DARK_RED)
                .append(Component.text(" Seek shelter immediately!", NamedTextColor.RED)));
        lobby.titlePlayers(ChatColor.RED + "STORM INCOMING", 80);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (BotBowsPlayer p : lobby.getPlayers()) {
                hazardTimers.put(p.player, new PlayerStormTimer(p, bars.get(p)).runTaskTimer(Main.getPlugin(), 0L, 2L));
            }
            Main.WORLD.setThundering(true);
            Main.WORLD.setStorm(true);
            Main.WORLD.setThunderDuration(12000); //10min
        }, 100L); // 5 sekunder
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

        final Player p;
        final BotBowsPlayer bp;
        final BossBar bar;
        int time = 0;
        public PlayerStormTimer(BotBowsPlayer bp, BossBar bar) {
            this.p = bp.player;
            this.bp = bp;
            this.bar = bar;
        }

        private boolean isPlayerExposed(Player p) {
            if (p.getLocation().getY() < GROUND_LEVEL) {return false;}
            if (p.getLocation().getY() >= UPPER_BOUND) {return true;}

            int x = (int) Math.floor(p.getLocation().getX());
            int y = (int) Math.floor(p.getLocation().getY());
            int z = (int) Math.floor(p.getLocation().getZ());
            //p.sendMessage(ChatColor.GRAY + "======== [DEBUG] ========\np_coord = " + p.getLocation().getX() + ", " + p.getLocation().getY() + ", " + p.getLocation().getZ());
            for (int i = y + 2; i < UPPER_BOUND + 2; i++) {
                //p.sendMessage(ChatColor.GRAY + "" + x + ", " + y + ", " + z + " : " + world.getBlockAt(x, y, z).getType());
                if (Main.WORLD.getBlockAt(x, i, z).getType() != Material.AIR) {return false;}
            }
            return true;
        }

        @Override
        public void run() { // annehver tick = 10Hz
            if (p.getGameMode() == GameMode.SPECTATOR) return; // if the player is dead, dont do anything
            if (isPlayerExposed(p)) {
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
                    Main.WORLD.strikeLightningEffect(p.getLocation());
                    time = 0; // resetter
                    bar.setProgress(0);
                    p.damage(0.5);
                    bp.die(Component.text(p.getName(), bp.getTeamColor())
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
