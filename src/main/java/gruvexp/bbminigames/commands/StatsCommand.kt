package gruvexp.bbminigames.commands

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private val ioRunner = CoroutineScope(Dispatchers.IO) // runs the async methods that can take some time bc db io

class StatsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val p = sender as Player

        val message = runCommandAndMessage(p, args)
        p.sendMessage(message)
        return true
    }

    private fun runCommandAndMessage(p: Player, args: Array<String>): TextComponent {
        if (args.isEmpty()) {
            return Component.text("You must specify subcommand!", NamedTextColor.RED)
        }
        val playerId = p.uniqueId
        when (args[0]) {
            "last_match" -> handleLastMatch(p)
        }
        return Component.text("literanno nottin")
    }

    private fun handleLastMatch(p: Player) {
        p.sendMessage(Component.text("Henter statistikk fra forrige kamp...", NamedTextColor.GRAY))
        val playerId = p.uniqueId

        ioRunner.launch {
            val result = Main.getPlugin().statsService.getLastMatchStats(playerId)

            if (result == null) {
                p.sendMessage(Component.text("Du har ikke spilt noen kamper ennå!", NamedTextColor.RED))
                return@launch
            }

            // Regn ut KD-ratio
            val kdRatio = if (result.playerDeaths == 0) {
                result.playerKills.toDouble()
            } else {
                result.playerKills.toDouble() / result.playerDeaths
            }

            // Lag klikkbart kartnavn-komponent med runCommand
            val mapNameString = result.map.name
            val mapComponent = Component.text(mapNameString, NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/botbows spectate $mapNameString"))
                .hoverEvent(HoverEvent.showText(Component.text("Klikk for å gå inn på kartet og se deg rundt!", NamedTextColor.AQUA)))

            // Bygg opp meldingsdesignet med ren Kotlin-syntaks
            var response = Component.text()
                .append(Component.text("\n=== STATISTIKK FOR FORRIGE KAMP ===\n", NamedTextColor.GOLD))
                .append(Component.text("Kart: ", NamedTextColor.YELLOW)).append(mapComponent).append(Component.text("\n"))
                .append(Component.text("Din score: ", NamedTextColor.YELLOW))
                .append(Component.text("${result.playerKills} Kills | ${result.playerDeaths} Deaths (K/D: ${String.format("%.2f", kdRatio)})\n", NamedTextColor.WHITE))
                .append(Component.text("Treff/Skade: ", NamedTextColor.YELLOW))
                .append(Component.text("${result.playerHits} treff | ${result.playerDamage} skade gitt\n\n", NamedTextColor.WHITE))

                .append(Component.text("Høydepunkter fra kampen:\n", NamedTextColor.AQUA))
                .append(Component.text("⚔️ Mest kills: ", NamedTextColor.YELLOW))
                .append(Component.text("${result.mostKillsCount} (${BotBows.getBotBowsPlayer(result.mostKillsPlayer).name})\n", NamedTextColor.WHITE))
                .append(Component.text("💀 Mest deaths: ", NamedTextColor.YELLOW))
                .append(Component.text("${result.mostDeathsCount} (${BotBows.getBotBowsPlayer(result.mostDeathsPlayer).name})\n", NamedTextColor.WHITE))
                .append(Component.text("💥 Mest skade: ", NamedTextColor.YELLOW))
                .append(Component.text("${result.mostDamageCount} (${BotBows.getBotBowsPlayer(result.mostDamagePlayer).name})\n\n", NamedTextColor.WHITE))

                .append(Component.text("Dine mest brukte evner:\n", NamedTextColor.LIGHT_PURPLE))
                .build()

            // Legg til topp 3 abilities ut fra enumen
            if (result.topAbilities.isEmpty()) {
                response = response.append(Component.text(" Ingen evner ble brukt.\n", NamedTextColor.GRAY))
            } else {
                result.topAbilities.forEachIndexed { index, (abilityType, uses) ->
                    response = response.append(Component.text(" #${index + 1} ${abilityType.name}: ", NamedTextColor.YELLOW))
                        .append(Component.text("$uses bruk\n", NamedTextColor.WHITE))
                }
            }

            response = response.append(Component.text("========================================\n", NamedTextColor.GOLD))

            // Adventure er trådsikkert, så vi kan sende den rett til spilleren herfra
            p.sendMessage(response)
        }
    }
}
