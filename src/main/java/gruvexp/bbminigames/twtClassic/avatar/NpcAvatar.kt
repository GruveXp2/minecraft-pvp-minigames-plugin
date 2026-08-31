package gruvexp.bbminigames.twtClassic.avatar

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar.ArmorSet
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mannequin
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import java.util.UUID
import kotlin.math.ceil

class NpcAvatar : BotBowsAvatar {
    private var mannequin: Mannequin
    private val _bp: BotBowsPlayer
    private var visualHp = -1
    override lateinit var teamManager: TeamManager

    constructor(mannequin: Mannequin, bp: BotBowsPlayer) {
        this.mannequin = mannequin
        _bp = bp
    }

    constructor(mannequin: Mannequin, previousAvatar: BotBowsAvatar) {
        this.mannequin = mannequin
        _bp = previousAvatar.bp
        teamManager = previousAvatar.teamManager
    }

    override fun message(component: Component) {
    }

    override val entity: LivingEntity
        get() =  mannequin
    override val bp: BotBowsPlayer
        get() = _bp

    override fun eliminate() {
        val newMannequin = Main.WORLD.spawn(bp.team.tribunePos, Mannequin::class.java).apply {
            profile = mannequin.profile
            customName(mannequin.customName())
            isCustomNameVisible = true
        }
        val oldId = uuid
        val newId = newMannequin.uniqueId
        BotBows.replacePlayerId(oldId, newId)
        mannequin.health = 0.0
        mannequin = newMannequin
        setInvis(true)
    }

    override fun revive() {
        setInvis(false)
    }

    override fun setHP(hp: Int) {
        visualHp = hp
        if (hp != 0) updateArmor()
    }

    override fun setMaxHP(maxHP: Int) {
        setHP(maxHP)
    }

    override val armor: ArmorSet
        get() {
            val armor = mannequin.equipment.armorContents
            return ArmorSet(armor[0], armor[1], armor[2], armor[3])
        }

    override fun equipFullArmor() {
        mannequin.equipment.armorContents = arrayOf(
            makeArmorPiece(Material.LEATHER_BOOTS),
            makeArmorPiece(Material.LEATHER_LEGGINGS),
            makeArmorPiece(Material.LEATHER_CHESTPLATE),
            makeArmorPiece(Material.LEATHER_HELMET)
        )
    }

    override fun destroy() {
        mannequin.remove()
    }

    override fun reset() {
    }

    override fun readyBattle(teamManager: TeamManager) {
        this.teamManager = teamManager
    }

    override fun setReady(ready: Boolean, itemIndex: Int) {
    }

    override val nextFreeSlot = 0

    override fun damage() {
        mannequin.damage(0.001)
        bp.effectManager.applyGlow(
            PlayerEffectManager.GlowSource.HIT_COOLDOWN,
            BotBows.HIT_DISABLED_ITEM_TICKS.toLong()
        )
        mannequin.isInvulnerable = true

        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { mannequin.isInvulnerable = false },
            BotBows.HIT_DISABLED_ITEM_TICKS.toLong()
        )
    }

    override var scale: Double
        get() = mannequin.getAttribute(Attribute.SCALE)!!.baseValue
        set(value) {
            mannequin.getAttribute(Attribute.SCALE)!!.baseValue = value
        }

    override fun setGlowing(flag: Boolean) {
        mannequin.isGlowing = flag
    }

    override fun addPotionEffect(effect: PotionEffect) {
        mannequin.addPotionEffect(effect)
    }

    override fun setColor(color: NamedTextColor) {
        teamManager.setColor(mannequin, color)
    }

    override val uuid: UUID
        get() = mannequin.uniqueId

    override val isSneaking
        get() = mannequin.isSneaking

    override fun updateSneakStamina(progress: Float) {
        if (progress >= 1) mannequin.isSneaking = false
    }

    override val headItem: ItemStack
        get() {
            val item = ItemStack(Material.PLAYER_HEAD)
            item.editMeta(SkullMeta::class.java) {
                it.displayName(bp.name.decoration(TextDecoration.ITALIC, false))
                val key = NamespacedKey(Main.getPlugin(), "uuid")
                it.persistentDataContainer.set(key, PersistentDataType.STRING, "${mannequin.uniqueId}")
                it.owningPlayer = Bukkit.getOfflinePlayer("Robotagz")
            }
            return item
        }

    override fun setItem(index: Int, item: ItemStack?) {
    }

    fun setInvis(invis: Boolean) {
        if (invis) {
            mannequin.equipment.clear()
        } else {
            updateArmor()
        }
    }

    override fun showTitle(title: Title) {
    }

    override fun showTitle(component: Component, seconds: Int) {
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
    }

    override fun initHazardBar(hazardType: HazardType, bar: BossBar) {
    }

    override fun setHazardBarProgress(hazardType: HazardType, progress: Float) {
    }

    private fun updateArmor() { // updates the armor pieces of the player
        val maxHP = bp.settings.maxHealth
        if (visualHp == maxHP) {
            equipFullArmor()
            return
        }
        val slots: Set<Int>
        if (maxHP > 5) {
            val d = maxHP / 5.0
            val i = ceil((maxHP - visualHp) / d).toInt()
            slots = BotBowsPlayer.HEALTH_ARMOR[3][i - 1]
        } else {
            slots = BotBowsPlayer.HEALTH_ARMOR[maxHP - 2][maxHP - visualHp - 1]
        }

        for (slot in slots) {
            when (slot) {
                0 -> mannequin.equipment.setBoots(null)
                1 -> mannequin.equipment.setLeggings(null)
                2 -> mannequin.equipment.setChestplate(null)
                3 -> mannequin.equipment.setHelmet(null)
            }
        }
    }

    private fun makeArmorPiece(material: Material): ItemStack {
        val armor = ItemStack(material)
        armor.editMeta(LeatherArmorMeta::class.java) { it.setColor(bp.team.dyeColor.color) }
        return armor
    }
}
