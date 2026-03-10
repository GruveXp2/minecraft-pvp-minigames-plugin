package gruvexp.bbminigames.api.damage

import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

sealed interface DamageContext {
    val type: DamageType

    fun getDisplayColor(): TextColor

    data class Player(val attacker: BotBowsPlayer, override val type: DamageType.Player) : DamageContext {
        override fun getDisplayColor(): TextColor = attacker.teamColor // Her har vi tilgang til spilleren!
    }

    data class Environment(override val type: DamageType.Environment, val color: TextColor) : DamageContext {
        override fun getDisplayColor(): TextColor = color // Her bruker vi fargen fra miljø-typen
    }

    fun formatMessage(defender: BotBowsPlayer): Component {
        val attackerColor: TextColor = getDisplayColor()
        val attackerColorLight = BotBows.lighten(attackerColor, 0.5)
        var msg = Component.text("", attackerColorLight)

        var template = type.template.replace("%d", defender.plainName)

        if (this is Player) {
            template = template.replace("%a", attacker.plainName)
        }

        return msg
    }
}