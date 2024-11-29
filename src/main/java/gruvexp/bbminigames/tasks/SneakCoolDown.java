package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.Bar;
import gruvexp.bbminigames.twtClassic.Cooldowns;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SneakCoolDown extends BukkitRunnable {

    Player p;
    int time = 20;
    public SneakCoolDown(Player p) {
        this.p = p;
    }

    @Override
    public void run() {
        if (p.isSneaking()) {
            if (time < 200) {
                time += 2;
                Cooldowns.sneakCooldowns.put(p, time);
                Bar.setSneakBarProgress(p, time/200d);
            } else {
                p.setSneaking(false);
                Bar.setSneakBarColor(p, ChatColor.RED, BarColor.RED);
            }
        } else {
            time -= 1;
            Cooldowns.sneakCooldowns.put(p, time);
            Bar.setSneakBarProgress(p, time/200d);
            if (time <=0) {
                cancel();
                Bar.setSneakBarColor(p, ChatColor.YELLOW, BarColor.YELLOW);
                Bar.setSneakBarVisibility(p, false);
            }
        }

    }
}
