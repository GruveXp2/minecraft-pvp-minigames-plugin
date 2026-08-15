package gruvexp.bbminigames.twtClassic.ability

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class AbilityEffect(val color: TextColor) {
    DAMAGE(NamedTextColor.RED),
    DEBUFF(NamedTextColor.YELLOW),
    BUFF(NamedTextColor.GREEN),
}