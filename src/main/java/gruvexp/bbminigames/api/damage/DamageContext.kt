package gruvexp.bbminigames.api.damage

import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

sealed interface DamageContext {
    val type: DamageType

    fun getMessageColor(): TextColor

    data class Player(val attacker: BotBowsPlayer, override val type: DamageType.Player) : DamageContext {
        override fun getMessageColor(): TextColor = BotBows.lighten(attacker.teamColor, 0.5)
    }

    data class Environment(override val type: DamageType.Environment, val color: TextColor) : DamageContext {
        override fun getMessageColor(): TextColor = color
    }

    fun formatMessage(defender: BotBowsPlayer): Component {
        val baseColor = getMessageColor()

        val tokens = type.template.split(Regex("(?=%d|%a)|(?<=%d|%a)")) // the regex gets the indices of where to split (right before and after %d or %a), so it basically splits but keeps the %a and %d
        var component = Component.text("", baseColor)

        for (part in tokens) {
            component = when (part) { // replaces %d and %a with defender and attacker name
                "%d" -> component.append(defender.name)
                "%a" -> component.append(if (this is Player) attacker.name else Component.text("<error: no attacker>", NamedTextColor.RED))
                else -> component.append(Component.text(part))
            }
        }
        return component
    }
}