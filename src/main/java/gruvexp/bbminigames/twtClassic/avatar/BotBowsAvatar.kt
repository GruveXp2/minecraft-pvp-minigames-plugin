package gruvexp.bbminigames.twtClassic.avatar

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.UUID

interface BotBowsAvatar {
    fun message(component: Component)
    val entity: LivingEntity
    val teamManager: TeamManager
    val bp: BotBowsPlayer
    fun eliminate()
    fun revive()
    fun setHP(hp: Int)
    fun setMaxHP(maxHP: Int)
    val armor: ArmorSet?
    fun equipFullArmor()
    fun destroy() // removes the player from the game
    fun reset() // its like removing and recreating this avatar, but reusing the object
    fun readyBattle(teamManager: TeamManager)
    fun setReady(ready: Boolean, itemIndex: Int)
    val nextFreeSlot: Int
    fun damage()
    var scale: Double
    fun setGlowing(flag: Boolean)
    fun addPotionEffect(effect: PotionEffect)
    fun setColor(color: NamedTextColor)
    val uuid: UUID
    val isSneaking: Boolean
    fun updateSneakStamina(progress: Float)
    val headItem: ItemStack
    fun setItem(index: Int, item: ItemStack?)
    val isOnGround: Boolean
        get() = entity.isOnGround

    fun setInvulnerable(invulnerable: Boolean) {
        entity.isInvulnerable = invulnerable
    }

    val location: Location
        get() = entity.location

    fun teleport(location: Location) {
        entity.teleport(location)
    }

    fun showTitle(title: Title)
    fun showTitle(component: Component, seconds: Int)
    fun playSound(location: Location, sound: String, volume: Float, pitch: Float)
    fun initHazardBar(hazardType: HazardType, bar: BossBar)
    fun setHazardBarProgress(hazardType: HazardType, progress: Float)

    @JvmRecord
    data class ArmorSet(
        val boots: ItemStack?,
        val leggings: ItemStack?,
        val chestplate: ItemStack?,
        val helmet: ItemStack?
    )
}
