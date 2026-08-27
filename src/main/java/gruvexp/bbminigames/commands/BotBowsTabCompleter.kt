package gruvexp.bbminigames.commands

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class BotBowsTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        val operations: MutableList<String> = mutableListOf(
            "finish_vote",
            "load_preset",
            "save_preset",
            "start",
            "stop",
            "leave"
        )
        if (sender is Player) {
            val bp = BotBows.getBotBowsPlayer(sender)
            if (bp != null && bp.lobby.settings.isPlayerMod(bp)) operations.add("transfer_mod")
        }
        if (args.size == 1) return operations
        val oper = args[0]
        when (oper) {
            "load_preset" -> {
                if (args.size == 2) return Main.getPlugin().presetService.getPresetNames().toList()
            }

            "save_preset" -> {
                if (args.size == 2) return listOf("<name>")
                if (args.size == 3) return Material.entries
                    .map { it.name.lowercase(Locale.getDefault()) }
                    .filter { "legacy" in it }
                    .filter { it.startsWith(args[2]) }
            }

            "transfer_mod" -> {
                if (args.size == 2 && sender is Player) {
                    val bp = BotBows.getBotBowsPlayer(sender)
                    return bp.lobby.getPlayers()
                        .filter { it !== bp }
                        .filter { it.avatar is PlayerAvatar }
                        .map { it.plainName }
                }
            }
        }
        return listOf()
    }
}
