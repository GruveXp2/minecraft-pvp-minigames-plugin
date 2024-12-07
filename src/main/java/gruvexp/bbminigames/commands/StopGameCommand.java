package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StopGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player p = (Player) sender;

        if (BotBows.activeGame) {
            BotBows.messagePlayers(Component.text("The game was ended by " + p.getName()));
            BotBows.botBowsGame.endGame();
        } else {
            p.sendMessage(Component.text("The game hasn't even started!", NamedTextColor.RED));
        }
        return true;
    }
}
