package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityContext.Melee
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnMelee
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SalmonSlap(bp: BotBowsPlayer, slot: Int) : Ability(bp, slot, AbilityType.SALMON_SLAP), OnMelee {
    override fun use() {
        bp.avatar.setItem(hotBarSlot, SALMON)
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { super.use() }, (20 * DURATION).toLong())
    }

    override fun trigger(ctx: Melee) {
        ctx.defender.damage(DamageContext.Player(DamageType.Player.SLAP, bp))
        registerSuccess()
    }

    companion object {
        const val DURATION: Int = 5 // seconds
        val SALMON: ItemStack = ItemStack(Material.SALMON)
    }
}
