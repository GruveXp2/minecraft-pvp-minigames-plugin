package gruvexp.bbminigames.commands

import gruvexp.bbminigames.twtClassic.BotBows
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SettingsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            return false
        }
        BotBows.accessSettings(sender)
        return true
    }
}
