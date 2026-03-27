package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class BotBowsTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("load_preset", "save_preset", "start", "stop", "leave");
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
                        .toList();
            }
        }
        return List.of();
    }
}
