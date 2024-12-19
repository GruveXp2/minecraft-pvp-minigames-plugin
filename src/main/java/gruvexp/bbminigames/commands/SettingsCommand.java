package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            p.sendMessage(Component.text("Cant change settings, the game is already ongoing!", NamedTextColor.RED));
        } else if (!BotBows.settings.isPlayerJoined(p)) {
            p.sendMessage(Component.text("You have to join to access the settings", NamedTextColor.RED));
        } else {
            BotBows.settings.mapMenu.open(p);
        }
        return true;
    }
}
