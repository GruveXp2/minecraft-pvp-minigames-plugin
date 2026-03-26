package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.model.preset.BattlePreset;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
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
            case "save_preset" -> {
                if (args.length == 1) {
                    return Component.text("You must specify a name for the preset!", NamedTextColor.RED);
                }
                String name = args[1];
                if (args.length == 2) {
                    return Component.text("You must specify an item icon for the preset!", NamedTextColor.RED);
                }
                Material icon = Material.getMaterial(args[2].toUpperCase());
                if (icon == null) return Component.text("Invalid item \"" + args[2] + "\"", NamedTextColor.RED);

                BattlePreset preset = lobby.settings.saveBattlePreset(name, icon);
                boolean success = Main.getPlugin().getPresetService().addPreset(preset);
                if (success) {
                    p.sendMessage(Component.text("Successfully added preset \"" + name + "\" with icon " + args[2]));
                } else {
                    p.sendMessage(Component.text("Failed to add preset: another preset with that name already exists!", NamedTextColor.RED));
                }
            }
            case "load_preset" -> {
                if (args.length == 1) {
                    return Component.text("You must spe/cify the preset to load!", NamedTextColor.RED);
                }
                String presetName = args[1];
                BattlePreset preset = Main.getPlugin().getPresetService().getPreset(presetName);
                if (preset == null) return Component.text("Error! No preset with name \"" + presetName + "\" exists");

                lobby.settings.applyBattlePreset(preset);
                p.sendMessage(Component.text("Successfully applied preset ")
                        .append(Component.text(presetName, NamedTextColor.AQUA)));
            }
            default -> {
                return Component.text("Invalid subcommand!", NamedTextColor.RED);
            }
        }
        return Component.empty();
    }
}
