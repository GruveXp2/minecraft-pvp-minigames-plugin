package gruvexp.bbminigames.api.ability

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Projectile

interface AbilityContext {
    data class EntityPlace(val loc: Location)

    data class Melee(val defender: BotBowsPlayer)

    data class Launch(val projectile: Projectile)

    data class BlockPlace(val block: Block, val face: BlockFace)
}
