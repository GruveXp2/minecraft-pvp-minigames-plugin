package gruvexp.bbminigames.twtClassic.ability

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import kotlin.math.cos
import kotlin.math.sin

abstract class PotionAbility protected constructor(bp: BotBowsPlayer, hotBarSlot: Int, type: AbilityType) :
    Ability(bp, hotBarSlot, type) {
    override fun use() {
        super.use()
        val nearbyPlayers = bp.getNearbyPlayers(RADIUS.toDouble())
            .filter { it.team == bp.team && it != bp }
            .toSet()
        applyPotionEffect(nearbyPlayers)

        nearbyPlayers.forEach { it.avatar.message(
            Component.text("Got ${effectDuration}s ", NamedTextColor.GREEN)
                .append(Component.text(effectName, NamedTextColor.DARK_GREEN))
                .append(Component.text(" effect from "))
                .append(bp.name)
        )}
    }

    protected abstract fun applyPotionEffect(players: Set<BotBowsPlayer>)

    protected abstract val effectName: String

    protected abstract val effectDuration: Int

    companion object {
        const val RADIUS: Int = 4

        fun createPotionRadiusEffect(bp: BotBowsPlayer) {
            val particleCount = 200
            val loc = bp.location.add(0.0, 0.1, 0.0)

            for (i in 0..particleCount) {
                val θ = 2 * Math.PI * i / particleCount
                val x = loc.x + RADIUS * cos(θ)
                val z = loc.z + RADIUS * sin(θ)

                val particleLoc = Location(loc.world, x, loc.y, z)
                bp.location.world.spawnParticle(
                    Particle.DUST,
                    particleLoc,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.4,
                    Particle.DustOptions(bp.team.dyeColor.color, 2.5f)
                )
            }
        }
    }
}
