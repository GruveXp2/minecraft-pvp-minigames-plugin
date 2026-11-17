package dev.gruve.minigames.botbows.ability.abilities

import dev.gruve.minigames.Main
import dev.gruve.minigames.botbows.ability.AbilityContext.Melee
import dev.gruve.minigames.botbows.ability.AbilityTrigger.OnMelee
import dev.gruve.minigames.botbows.damage.DamageContext
import dev.gruve.minigames.botbows.damage.DamageType
import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.Ability
import dev.gruve.minigames.botbows.ability.AbilityType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SalmonSlap(bp: BotBowsPlayer, slot: Int) : Ability(bp, slot, AbilityType.SALMON_SLAP), OnMelee {
    override fun use() {
        bp.avatar.setItem(hotBarSlot, SALMON)
        Bukkit.getScheduler().runTaskLater(Main.plugin, Runnable { super.use() }, (20 * DURATION).toLong())
    }

    override fun trigger(ctx: Melee) {
        val successful = ctx.defender.damage(DamageContext.Player(DamageType.Player.SLAP, bp))
        if (successful) registerSuccess()
    }

    companion object {
        const val DURATION: Int = 5 // seconds
        val SALMON = ItemStack(Material.SALMON)
    }
}
