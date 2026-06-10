package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Util;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.List;

public class TestTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1)
            return List.of("5_bots", "vote", "tb", "add_bot", "print_eq", "add_spinning", "init_wheel", "register_blocks", "t", "w", "a", "b", "t1", "t2", "ta", "give_ability_items", "toggle_debugging", "inv", "set_blaze_rod_cooldown", "test_arc");

        Player p = (Player) sender;
        String oper = args[0];
        if (oper.equals("test_arc")) {
            if (args.length <= 4) {
                Vector3i loc1 = Util.getTargetBlockLoc(p, 10);
                return List.of(Util.print(loc1));
            }
            if (args.length <= 7) {
                Vector3i loc2 = Util.getTargetBlockLoc(p, 10);
                return List.of(Util.print(loc2));
            }
        } else if (oper.equals("vote")) {
            if (args.length == 2) {
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
                return bp.lobby.getPlayers().stream().map(BotBowsPlayer::getPlainName).map(name -> name.replace(" ", "_")).toList();
            } else if (args.length == 3) {
                return Arrays.stream(BotBowsMap.values()).map(map -> map.name().toLowerCase()).toList();
            }
        }
        return List.of("");
    }
}
