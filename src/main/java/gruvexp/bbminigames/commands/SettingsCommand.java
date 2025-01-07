package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SettingsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) {
            p.sendMessage(Component.text("You have to join to access the settings", NamedTextColor.RED));
            return true;
        }
        if (lobby.isGameActive()) {
            p.sendMessage(Component.text("Cant change settings, the game is already ongoing!", NamedTextColor.RED));
            return true;
        }
        BotBows.getLobby(p).settings.mapMenu.open(p);
        return true;
    }
}
