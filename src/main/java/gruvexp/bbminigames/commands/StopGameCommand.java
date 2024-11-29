package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        if (BotBows.activeGame) {
            BotBows.messagePlayers("The game was ended by " + p.getPlayerListName());
            BotBows.botBowsGame.endGame();
        } else {
            p.sendMessage(ChatColor.RED + "The game hasn't even started!");
        }
        return true;
    }
}
