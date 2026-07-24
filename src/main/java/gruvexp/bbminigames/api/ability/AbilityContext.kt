package gruvexp.bbminigames.api.ability

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Projectile

interface AbilityContext {
    data class EntityPlace(@JvmField val loc: Location)

    data class Melee(@JvmField val defender: BotBowsPlayer)

    data class Launch(@JvmField val projectile: Projectile)

    data class BlockPlace(@JvmField val block: Block, @JvmField val face: BlockFace)
}
