package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.Main;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) { //starter spillet

        Player p = (Player) sender;

        if (SumoManager.activeBattle == 1) {
            p.sendMessage(ChatColor.RED + "There is already an active battle!");
            return true;
        }

        int players = SumoData.playerList.size();
        if (players < 2) {
            p.sendMessage(ChatColor.RED + "Not enough players! You need " + (2 - players) + " more.");
        } else {
            SumoData.init(); //iniskier noen variabler
            p.sendMessage("Starting tournament " + players + "v" + players);
            Bar.CreateBar("Game starting soon!_", BarColor.GREEN, BarStyle.SEGMENTED_12, 0);
            new PreBattle().runTaskTimer(Main.getPlugin(), 0L, 1L); //period 2T
        }

        return true;
    }
}
