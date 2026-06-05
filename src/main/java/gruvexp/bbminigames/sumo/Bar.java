package gruvexp.bbminigames.sumo;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Bar {

    private static final ArrayList<BossBar> bar_list = new ArrayList<>();

    public static BossBar GetBar(int index) {
        return bar_list.get(index);
    }


    public static void Modify(BossBar bar, String title, BarColor color, BarStyle style) {
        bar.setTitle(title);
        bar.setColor(color);
        bar.setStyle(style);
    }
    public static void SetVisible(BossBar bar, boolean bool) {
        bar.setVisible(bool);
    }
    public static void SetProgress(BossBar bar, double progress) {
        bar.setProgress(progress);
    }
    public static void CreateBar(String title, BarColor color, BarStyle style, int index) {
        BossBar bar = Bukkit.createBossBar(title, color, style);
        for (Player player:Bukkit.getOnlinePlayers()) {
            bar.addPlayer(player);
        }
        bar_list.add(index, bar);
    }

}
