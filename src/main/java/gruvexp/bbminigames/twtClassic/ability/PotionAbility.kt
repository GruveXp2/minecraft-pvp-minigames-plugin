package gruvexp.bbminigames.twtClassic.ability

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

abstract class PotionAbility protected constructor(bp: BotBowsPlayer, hotBarSlot: Int, type: AbilityType) :
    Ability(bp, hotBarSlot, type) {
    override fun use() {
        super.use()
        val nearbyPlayers = bp.getNearbyPlayers(RADIUS.toDouble())
            .filter { it.team == this.bp.team && it != bp }
            .toSet()
        applyPotionEffect(nearbyPlayers)

        nearbyPlayers.forEach { it.avatar.message(
            Component.text("Got ${this.effectDuration}s ", NamedTextColor.GREEN)
                .append(Component.text(this.effectName, NamedTextColor.DARK_GREEN))
                .append(Component.text(" effect from "))
                .append(bp.name)
        )}
    }

    protected abstract fun applyPotionEffect(players: Set<BotBowsPlayer>)

    protected abstract val effectName: String

    protected abstract val effectDuration: Int

    companion object {
        const val RADIUS: Int = 4
    }
}
