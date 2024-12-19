package gruvexp.bbminigames.twtClassic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Bar {

    public static HashMap<Player, BossBar> sneakBars = new HashMap<>(2);

    public static void sneakBarInit() {
        for (BotBowsPlayer p : BotBows.getPlayers()) {
            BossBar bar = Bukkit.createBossBar(ChatColor.YELLOW + "Sneaking cooldown", BarColor.YELLOW, BarStyle.SEGMENTED_10);
            bar.addPlayer(p.player);
            bar.setProgress(0d);
            bar.setVisible(false);
            sneakBars.put(p.player, bar);
        }
    }

    public static void setSneakBarColor(Player p, ChatColor chatColor, BarColor barColor) {
        sneakBars.get(p).setTitle(chatColor + sneakBars.get(p).getTitle().strip());
        sneakBars.get(p).setColor(barColor);
    }

    public static void setSneakBarVisibility(Player p, boolean bool) {
        sneakBars.get(p).setVisible(bool);
    }

    public static void setSneakBarProgress(Player p, double progress) {
        sneakBars.get(p).setProgress(progress);
    }
}
