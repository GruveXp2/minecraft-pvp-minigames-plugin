package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeaveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Player p = (Player) sender;
        BotBows.leaveGame(p);
        return true;
    }
}
