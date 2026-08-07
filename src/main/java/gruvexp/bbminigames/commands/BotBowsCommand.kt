package gruvexp.bbminigames.commands

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class BotBowsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val p = sender as Player

        val message = runCommandAndMessage(p, args)
        p.sendMessage(message)
        return true
    }

    private fun runCommandAndMessage(p: Player, args: Array<String>): TextComponent {
        if (args.isEmpty()) return Component.text("You must specify subcommand!", NamedTextColor.RED)

        val bp = BotBows.getBotBowsPlayer(p) ?: return Component.text("You must be in a BotBows lobby to perform this command!", NamedTextColor.RED)
        val lobby = bp.lobby
        when (args[0]) {
            "start" -> lobby.startGame(p) // TODO: take in bp instead?

            "stop" -> {
                val game = lobby.botBowsGame ?: return Component.text("The game hasn't even started!", NamedTextColor.RED)
                game.endGame(bp)
            }
            "leave" -> lobby.leaveGame(p)

            "save_preset" -> {
                if (args.size == 1) return Component.text("You must specify a name for the preset!", NamedTextColor.RED)

                val name = args[1]
                if (args.size == 2) return Component.text("You must specify an item icon for the preset!", NamedTextColor.RED)

                val icon = Material.getMaterial(args[2].uppercase(Locale.getDefault()))
                    ?: return Component.text("Invalid item \"${args[2]}\"", NamedTextColor.RED)

                val preset = lobby.settings.saveBattlePreset(name, icon)
                val success = Main.getPlugin().presetService.addPreset(preset)
                if (success) {
                    lobby.settings.presetsMenu.displayPresets()
                    p.sendMessage(Component.text("Successfully added preset \"$name\" with icon ${args[2]}"))
                } else {
                    p.sendMessage(Component.text("Failed to add preset: another preset with that name already exists!", NamedTextColor.RED))
                }
            }
            "load_preset" -> {
                if (args.size == 1) return Component.text("You must specify the preset to load!", NamedTextColor.RED)

                val presetName = args[1]
                val preset = Main.getPlugin().presetService.getPreset(presetName)
                    ?: return Component.text("Error! No preset with name \"$presetName\" exists")

                if (!lobby.settings.isPlayerMod(bp)) return Component.text("Only mods can load presets")

                lobby.settings.applyBattlePreset(preset)
            }
            "transfer_mod" -> {
                if (!bp.lobby.settings.isPlayerMod(bp)) return Component.text("Only mods can transfer their mod role (bruh)")

                val otherPlayerName = args[1]
                val otherPlayer = Bukkit.getPlayer(otherPlayerName)
                    ?: return Component.text("That player doesnt exist!", NamedTextColor.RED)

                val otherBp = lobby.getBotBowsPlayer(otherPlayer)
                    ?: return Component.text("That player isnt in this lobby!", NamedTextColor.RED)

                lobby.settings.setModPlayer(otherBp)
            }
            "finish_vote" -> bp.lobby.settings.finishVoting()

            else -> return Component.text("Invalid subcommand!", NamedTextColor.RED)
        }
        return Component.empty()
    }
}
