package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotBowsTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> operations = new ArrayList<>(List.of("finish_vote", "load_preset", "save_preset", "start", "stop", "leave"));
        if (sender instanceof Player p) {
            BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
            if (bp != null && bp.lobby.settings.isPlayerMod(bp)) operations.add("transfer_mod");
        }
        if (args.length == 1) return operations;
        String oper = args[0];
        switch (oper) {
            case "load_preset" -> {
                if (args.length == 2) return Main.getPlugin().getPresetService().getPresetNames().stream().toList();
            }
            case "save_preset" -> {
                if (args.length == 2) return List.of("<name>");
                if (args.length == 3) return Arrays.stream(Material.values())
                        .map(e -> e.name().toLowerCase())
                        .filter(s -> !s.contains("legacy"))
                        .filter(s -> s.startsWith(args[2]))
                        .toList();
            }
            case "transfer_mod" -> {
                if (args.length == 2 && sender instanceof Player p) {
                    BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
                    return bp.lobby.getPlayers().stream()
                            .filter(teamMember -> teamMember != bp)
                            .filter(teamMember -> teamMember.avatar instanceof PlayerAvatar)
                            .map(BotBowsPlayer::getPlainName)
                            .toList();
                }
            }
        }
        return List.of();
    }
}
