package gruvexp.bbminigames.commands

import gruvexp.bbminigames.Util
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class TestTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): MutableList<String> {
        if (args.size == 1) return mutableListOf(
            "end_round",
            "5_bots",
            "vote",
            "tb",
            "db",
            "add_bot",
            "print_eq",
            "add_spinning",
            "init_wheel",
            "register_blocks",
            "t",
            "w",
            "a",
            "b",
            "t1",
            "t2",
            "ta",
            "give_ability_items",
            "toggle_debugging",
            "inv",
            "set_blaze_rod_cooldown",
            "test_arc"
        )

        val p = sender as Player
        val oper = args[0]
        if (oper == "test_arc") {
            if (args.size <= 4) {
                val loc1 = Util.getTargetBlockLoc(p, 10)
                return mutableListOf(Util.print(loc1))
            }
            if (args.size <= 7) {
                val loc2 = Util.getTargetBlockLoc(p, 10)
                return mutableListOf(Util.print(loc2))
            }
        } else if (oper == "vote") {
            if (args.size == 2) {
                val bp = BotBows.getBotBowsPlayer(p)
                return bp.lobby.getPlayers()
                    .map { it.plainName }
                    .map { name -> name.replace(" ", "_") }
                    .toMutableList()
            } else if (args.size == 3) {
                return BotBowsMap.entries.map { it.name.lowercase() }.toMutableList()
            }
        }
        return mutableListOf("")
    }
}
