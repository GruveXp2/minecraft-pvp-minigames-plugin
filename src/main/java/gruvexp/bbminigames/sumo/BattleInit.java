package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BattleInit extends BukkitRunnable {

    Main plugin;

    public BattleInit(Main plugin) {
        this.plugin = plugin;
    }
    int time = 0;
    Player[] players;

    @Override
    public void run() {
        if (time > 33) {
            cancel();
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();


        switch (time) { //gjør at det kommer 321-GO title.
            case 0://gjør sånn at serveren ikke blir spamma av commands
                //Bukkit.broadcastMessage("BattleInit.java");
                Bukkit.dispatchCommand(console, "gamerule logAdminCommands false");
                players = SumoData.getBattle();
                for (Player p:players) {
                    p.sendMessage("Battle starting in 3..");
                }
                break;
            case 12:
                Bukkit.dispatchCommand(console, SumoData.frame(1, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(1, "south"));
                for (Player p:players) {
                    p.sendMessage("Battle starting in 2..");
                }
                break;
            case 20:
                Bukkit.dispatchCommand(console, SumoData.frame(2, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(2, "south"));
                for (Player p:players) {
                    p.sendMessage("Battle starting in 1..");
                }
                break;
            case 28:
                Bukkit.dispatchCommand(console, SumoData.frame(3, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(3, "south"));
                for (Player p:players) {
                    p.sendMessage("GO!");
                }
                break;
            case 29:
                Bukkit.dispatchCommand(console, SumoData.frame(4, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(4, "south"));
                break;
            case 30:
                Bukkit.dispatchCommand(console, SumoData.frame(5, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(5, "south"));
                break;
            case 31:
                Bukkit.dispatchCommand(console, SumoData.frame(6, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(6, "south"));
                break;
            case 32:
                Bukkit.dispatchCommand(console, SumoData.frame(7, "north"));
                Bukkit.dispatchCommand(console, SumoData.frame(7, "south"));
                Bukkit.dispatchCommand(console, "gamerule logAdminCommands true");
        }
        time++;
    }

}
