package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.twtClassic.BarManager;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Cooldowns;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SneakCoolDown extends BukkitRunnable {

    Player p;
    final BarManager barManager;
    int time = 20;
    public SneakCoolDown(Player p) {
        this.p = p;
        barManager = BotBows.getLobby(p).botBowsGame.barManager;
    }

    @Override
    public void run() {
        if (p.isSneaking()) {
            if (time < 200) {
                time += 2;
                Cooldowns.sneakCooldowns.put(p, time);
                BotBows.getLobby(p).botBowsGame.barManager.setSneakBarProgress(p, time/200d);
            } else {
                p.setSneaking(false);
                barManager.setSneakBarColor(p, ChatColor.RED, BarColor.RED);
            }
        } else {
            time -= 1;
            Cooldowns.sneakCooldowns.put(p, time);
            barManager.setSneakBarProgress(p, time/200d);
            if (time <=0) {
                cancel();
                barManager.setSneakBarColor(p, ChatColor.YELLOW, BarColor.YELLOW);
                barManager.setSneakBarVisibility(p, false);
            }
        }

    }
}
