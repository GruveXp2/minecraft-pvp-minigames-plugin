package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BotBowsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player p = (Player) sender;

        TextComponent message = runCommandAndMessage(p, args);
        p.sendMessage(message);
        return true;
    }

    private TextComponent runCommandAndMessage(Player p, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return Component.text("You must specify subcommand!", NamedTextColor.RED);
        }
        BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
        if (bp == null) {
            return Component.text("You must be in a BotBows lobby to perform this command!", NamedTextColor.RED);
        }
        Lobby lobby = bp.lobby;
        switch (args[0]) {
            case "start" -> lobby.startGame(p);
            case "stop" -> {
                if (lobby.isGameActive()) {
                    lobby.botBowsGame.endGame(bp);
                } else {
                    return Component.text("The game hasn't even started!", NamedTextColor.RED);
                }
            }
            case "leave" -> lobby.leaveGame(p);
            default -> {
                return Component.text("Invalid subcommand!", NamedTextColor.RED);
            }
        }
        return Component.empty();
    }
}
