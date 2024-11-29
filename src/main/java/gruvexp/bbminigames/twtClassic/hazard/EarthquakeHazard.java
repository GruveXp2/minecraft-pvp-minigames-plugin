package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.PlayerEarthQuakeTimer;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;

public class EarthquakeHazard extends Hazard{
    static HashMap<BotBowsPlayer, BossBar> bars = new HashMap<>(2);

    public EarthquakeHazard(Settings settings) {
        super(settings);
    }

    public void init() { // calles når spillet begynner
        if (getHazardChance() == HazardChance.DISABLED) return;
        for (BotBowsPlayer p : settings.getPlayers()) {
            BossBar bar = Bukkit.createBossBar(ChatColor.GOLD + "Anvil timer", BarColor.YELLOW, BarStyle.SEGMENTED_6);
            bar.addPlayer(p.PLAYER);
            bar.setProgress(0d);
            bar.setVisible(false);
            bars.put(p, bar);
        }
    }
    @Override
    protected void trigger() {
        BotBows.messagePlayers(ChatColor.DARK_RED + "EARTHQUAKE INCOMING!" + ChatColor.RED + " Stay above ground!");
        BotBows.titlePlayers(ChatColor.RED + "EARTHQUAKE INCOMING", 80);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (BotBowsPlayer p : settings.getPlayers()) {
                hazardTimers.put(p.PLAYER, new PlayerEarthQuakeTimer(p, bars.get(p)).runTaskTimer(Main.getPlugin(), 0L, 2L));
            }
        }, 100L); // 5 sekunder
    }
    @Override
    public void end() {
        super.end();
        for (BossBar bossBar : bars.values()) { // resett storm baren og skjul den
            bossBar.setVisible(false);
            bossBar.setProgress(0d);
        }
        hazardTimers.clear();
    }
}
