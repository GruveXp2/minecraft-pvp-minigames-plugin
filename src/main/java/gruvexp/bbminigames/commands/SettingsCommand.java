package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }
        // grabs which player did the command. endrer datatype til Player
        if (BotBows.activeGame) {
            p.sendMessage(ChatColor.RED + "Cant change settings, the game is already ongoing!");
        } else if (!BotBows.settings.isPlayerJoined(p)) {
            p.sendMessage(ChatColor.RED + "You have to join to access the settings");
        } else {
            BotBows.mapMenu.open(p);
        }
        return true;
    }
}
