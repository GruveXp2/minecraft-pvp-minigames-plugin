package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEarthQuakeTimer extends BukkitRunnable {

    static final int GROUND_LEVEL = 22;
    static final int UPPER_BOUND = 29;
    static final int SECONDS = 6; // hvor lenge man kan stå før man blir tatt av lightning

    final Player p;
    final BotBowsPlayer bp;
    final BossBar bar;
    int time = 0;
    public PlayerEarthQuakeTimer(BotBowsPlayer bp, BossBar bar) {
        this.p = bp.PLAYER;
        this.bp = bp;
        this.bar = bar;
    }

    private boolean isPlayerUnderground(Player p) {
        if (p.getLocation().getY() >= GROUND_LEVEL) {return false;}

        int x = (int) Math.floor(p.getLocation().getX());
        int y = (int) Math.floor(p.getLocation().getY());
        int z = (int) Math.floor(p.getLocation().getZ());
        //p.sendMessage(ChatColor.GRAY + "======== [DEBUG] ========\np_coord = " + p.getLocation().getX() + ", " + p.getLocation().getY() + ", " + p.getLocation().getZ());
        for (int i = y + 2; i < UPPER_BOUND + 2; i++) {
            //p.sendMessage(ChatColor.GRAY + "" + x + ", " + y + ", " + z + " : " + world.getBlockAt(x, y, z).getType());
            if (Main.WORLD.getBlockAt(x, i, z).getType() != Material.AIR) {return true;}
        }
        return false;
    }

    @Override
    public void run() { // annehver tick = 10Hz
        if (p.getGameMode() == GameMode.SPECTATOR) return; // if the player is dead, dont do anything
        if (isPlayerUnderground(p)) {
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
                FallingBlock fallingAnvil = Main.WORLD.spawnFallingBlock(p.getLocation().add(0, 3.9, 0), Material.ANVIL.createBlockData());
                fallingAnvil.setHurtEntities(true);
                fallingAnvil.setDropItem(false);
                time = 0; // resetter
                bar.setProgress(0);
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    p.damage(1); // sånn
                    bp.die(bp.getTeam().COLOR + p.getPlayerListName() + ChatColor.GOLD + " was squashed by a stone the size of a large boulder!");
                }, 20L);
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
