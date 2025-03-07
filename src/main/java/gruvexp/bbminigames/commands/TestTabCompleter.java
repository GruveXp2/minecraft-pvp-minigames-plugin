package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;

public class TestTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1)
            return List.of("t", "w", "a", "b", "t1", "t2", "give_ability_items", "toggle_debugging", "inv", "set_blaze_rod_cooldown", "test_arc");

        Player p = (Player) sender;
        String oper = args[0];
        if (oper.equals("test_arc")) {
            if (args.length <= 4) {
                Vector3i loc1 = Utils.getTargetBlock(p, 10);
                return List.of(Utils.print(loc1));
            }
            if (args.length <= 7) {
                Vector3i loc2 = Utils.getTargetBlock(p, 10);
                return List.of(Utils.print(loc2));
            }
        }
        return List.of("");
    }
}
