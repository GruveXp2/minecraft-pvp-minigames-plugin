package gruvexp.bbminigames.twtClassic.hazard

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.scheduler.BukkitRunnable

abstract class Hazard protected constructor(val type: HazardType) {
    var chance: HazardChance = type.defaultChance
    var isActive: Boolean = false
        private set

    var hazardTimers = mutableMapOf<BotBowsPlayer, BukkitRunnable>()

    fun triggerOnChance(players: Set<BotBowsPlayer>) {
        if (chance.occurs()) {
            isActive = true
            announce(players)
            trigger(players)
        }
    }

    abstract fun init(players: Set<BotBowsPlayer>)

    protected abstract fun trigger(players: Set<BotBowsPlayer>) // hazarden starter
    protected abstract val announceMessage: HazardMessage

    private fun announce(players: Set<BotBowsPlayer>) {
        val msg = announceMessage
        players.forEach {
            it.avatar.message(
                Component.text(msg.chatHeader, NamedTextColor.DARK_RED)
                    .append(Component.text(" ${msg.chatDescription}", NamedTextColor.RED))
            )
            it.avatar.showTitle(Component.text(msg.screenTitle, NamedTextColor.RED), 4)
        }
    }

    abstract val name: String

    abstract val description: Array<TextComponent>

    abstract val actionDescription: String

    open fun end() {
        hazardTimers.values.forEach { it.cancel() }
        hazardTimers.clear()
        isActive = false
    }

    data class HazardMessage(
        val chatHeader: String,
        val chatDescription: String,
        val screenTitle: String
    )
}
