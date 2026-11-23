package dev.gruve.minigames.botbows.avatar

import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.hazard.HazardType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.UUID
abstract class BotBowsAvatar(
    val bp: BotBowsPlayer,
) {
    abstract val entity: LivingEntity
    protected var visualHp = -1
    protected var visualMaxHp = -1
    lateinit var teamManager: TeamManager
    abstract val armor: ArmorSet
    abstract val nextFreeSlot: Int
    var scale: Double
        get() = entity.getAttribute(Attribute.SCALE)!!.baseValue
        set(value) {
            entity.getAttribute(Attribute.SCALE)!!.baseValue = value
        }
    val uuid: UUID
        get() = entity.uniqueId
    val isSneaking: Boolean
        get() = entity.isSneaking
    abstract val headItem: ItemStack

    val isOnGround: Boolean
        get() = entity.isOnGround

    val location: Location
        get() = entity.location

    fun setInvulnerable(invulnerable: Boolean) {
        entity.isInvulnerable = invulnerable
    }
    abstract fun message(component: Component)
    abstract fun eliminate()
    abstract fun revive()
    fun setHp(hp: Int) {
        visualHp = hp
        onHpChange()
        if (hp != 0) updateArmor()
    }
    fun setMaxHp(maxHp: Int) {
        visualMaxHp = maxHp
        onMaxHpChange()
    }
    open fun onMaxHpChange() {}
    open fun onHpChange() {}
    abstract fun setSpeed(speed: Int)
    abstract fun equipFullArmor()
    abstract fun destroy() // removes the player from the game
    abstract fun reset() // its like removing and recreating this avatar, but reusing the object
    abstract fun readyBattle(teamManager: TeamManager)
    abstract fun setReady(ready: Boolean, itemIndex: Int)
    abstract fun damage()
    abstract fun setGlowing(flag: Boolean)
    abstract fun addPotionEffect(effect: PotionEffect)
    abstract fun setColor(color: NamedTextColor)
    abstract fun updateSneakStamina(progress: Float)
    abstract fun setItem(index: Int, item: ItemStack?)
    abstract fun showTitle(title: Title)
    fun teleport(location: Location) {
        entity.teleport(location)
    }
    abstract fun showTitle(component: Component, seconds: Int)
    abstract fun playSound(location: Location, sound: String, volume: Float, pitch: Float)
    abstract fun initHazardBar(hazardType: HazardType, bar: BossBar)
    abstract fun setHazardBarProgress(hazardType: HazardType, progress: Float)

    abstract fun updateArmor()

    data class ArmorSet(
        val boots: ItemStack?,
        val leggings: ItemStack?,
        val chestplate: ItemStack?,
        val helmet: ItemStack?
    )
}
