package gruvexp.bbminigames.api.damage

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

sealed interface DamageType {
    val template: String

    // damaged by the environment
    enum class Environment(override val template: String, val messageColor: TextColor) : DamageType {
        LAVA("%d tried to swim in lava", NamedTextColor.GOLD),
        LIGHTNING("%d was electrocuted to a crisp!", NamedTextColor.AQUA),
        EARTHQUAKE("%d was squashed by a small stone the size of a large boulder", NamedTextColor.GOLD),
        GHOST("%d was ghosted", NamedTextColor.DARK_GRAY);
    }

    // damaged by a player
    enum class Player(override val template: String) : DamageType {
        BOW("%d was sniped by %a"),
        BUBBLE_JET("%d was hit by %a's bubble jet"),
        SLAP("%d was slapped by %a"),
        COOL_ROD("%d got slapped by %a's cool rod"),
        SPLASH_BOW("%d was splashed by %a"),
        THUNDER_BOW("%d was electrocuted by %a"),
        THUNDER_BOW_CHAIN("%d was chain zapped by %a"),
        CREEPER("%d hugged %a's creeper"),
        LASER("%d got enlightened by %a's laser beam");
    }
}