package gruvexp.bbminigames.twtClassic.avatar

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Lobby
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar.ArmorSet
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import java.time.Duration
import kotlin.math.ceil

class PlayerAvatar : BotBowsAvatar {
    private val player: Player
    private val _bp: BotBowsPlayer
    private val sneakBar: BossBar
    private val hazardBars  = mutableMapOf<HazardType, BossBar>()
    private var visualHp = 0
    override lateinit var teamManager: TeamManager

    constructor(player: Player, bp: BotBowsPlayer) {
        this.player = player
        _bp = bp
        sneakBar = BossBar.bossBar(Component.text("Sneaking cooldown"), 0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_10)
        player.gameMode = GameMode.ADVENTURE
    }

    constructor(player: Player, previousAvatar: BotBowsAvatar) {
        this.player = player
        _bp = previousAvatar.bp
        sneakBar = BossBar.bossBar(Component.text("Sneaking cooldown"), 0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_10)
        teamManager = previousAvatar.teamManager
    }

    override fun message(component: Component) {
        player.sendMessage(component)
    }

    override val entity: LivingEntity
        get() = player
    override val bp
        get() = _bp

    override fun eliminate() {
        player.gameMode = GameMode.SPECTATOR
    }

    override fun revive() {
        player.gameMode = GameMode.ADVENTURE
    }

    override fun setHP(hp: Int) {
        visualHp = hp
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            player.health = 1.0 // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            player.health = hp * 2.0 // halve hjerter
            updateArmor()
        }
    }

    override fun setMaxHP(maxHP: Int) {
        player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = maxHP * 2.0
        setHP(maxHP)
    }

    override val armor: ArmorSet
        get() {
        val armor = player.inventory.armorContents
        return ArmorSet(armor[0], armor[1], armor[2], armor[3])
    }

    override fun equipFullArmor() {
        player.inventory.armorContents = arrayOf(
            makeArmorPiece(Material.LEATHER_BOOTS),
            makeArmorPiece(Material.LEATHER_LEGGINGS),
            makeArmorPiece(Material.LEATHER_CHESTPLATE),
            makeArmorPiece(Material.LEATHER_HELMET)
        )
    }

    override fun destroy() {
        reset()
        player.inventory.setItem(0, BotBows.MENU_ITEM)
    }

    override fun reset() {
        player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
        player.inventory.clear()
        player.isGlowing = false
        player.isInvulnerable = false
        player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = 20.0
        player.gameMode = GameMode.SPECTATOR
        hazardBars.values.forEach { it.removeViewer(player) }
        sneakBar.removeViewer(player)
    }

    override fun readyBattle(teamManager: TeamManager) {
        player.inventory.remove(Lobby.READY.clone()) // removes ready up item
        this.teamManager = teamManager
    }

    override fun setReady(ready: Boolean, itemIndex: Int) {
        player.inventory.setItem(itemIndex, Lobby.LOADING)
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { player.inventory.setItem(itemIndex, if (ready) Lobby.READY else Lobby.NOT_READY) },
            2L
        )
    }

    override val nextFreeSlot: Int
        get() {
        val inv = player.inventory
        for (slot in 1..8) {
            if (inv.getItem(slot) == null) {
                return slot
            }
        }
        return -1
    }

    override fun damage() {
        player.damage(0.001)
        bp.effectManager.applyGlow(
            PlayerEffectManager.GlowSource.HIT_COOLDOWN,
            BotBows.HIT_DISABLED_ITEM_TICKS.toLong()
        )
        player.isInvulnerable = true

        val inv = player.inventory
        for (i in 0..8) { // fills the hotbar with barriers to show that they cant shoot or use abilities (and moves prev items up into inventory as buffer)
            if (inv.getItem(i) == null) continue

            inv.setItem(i + 27, inv.getItem(i))
            inv.setItem(i, ItemStack(Material.BARRIER))
        }

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            player.isInvulnerable = false
            for (i in 0..8) { // moving items back
                val item = inv.getItem(i + 27)
                inv.setItem(i, item)
            }
        }, BotBows.HIT_DISABLED_ITEM_TICKS.toLong())
    }

    override var scale: Double
        get() = player.getAttribute(Attribute.SCALE)!!.baseValue
        set(value) {
            player.getAttribute(Attribute.SCALE)!!.baseValue = value
        }

    override fun setGlowing(flag: Boolean) {
        player.isGlowing = flag
    }

    override fun addPotionEffect(effect: PotionEffect) {
        player.addPotionEffect(effect)
    }

    override fun setColor(color: NamedTextColor) {
        teamManager.setColor(player, color)
    }

    override val uuid
        get() = player.uniqueId
    override val isSneaking
        get() = player.isSneaking

    override fun updateSneakStamina(progress: Float) {
        val isExhausted = bp.isSneakingExhausted
        sneakBar.progress(progress)
        if (progress > 0) player.showBossBar(sneakBar) else player.hideBossBar(sneakBar)
        sneakBar.color(if (isExhausted) BossBar.Color.RED else BossBar.Color.YELLOW)
        sneakBar.name(Component.text("Sneaking", if (isExhausted) NamedTextColor.RED else NamedTextColor.YELLOW))
        if (progress >= 1 && isExhausted) player.isSneaking = false
    }

    override val headItem: ItemStack
        get() {
        val item = ItemStack(Material.PLAYER_HEAD)
        item.editMeta(SkullMeta::class.java) {
            it.displayName(bp.name.decoration(TextDecoration.ITALIC, false))
            it.persistentDataContainer.set(
                NamespacedKey(Main.getPlugin(), "uuid"),
                PersistentDataType.STRING,
                player.uniqueId.toString()
            )
            it.owningPlayer = Bukkit.getPlayer(player.name)
        }
        return item
    }

    override fun setItem(index: Int, item: ItemStack?) {
        player.inventory.setItem(index, item)
    }

    override fun showTitle(title: Title) {
        player.showTitle(title)
    }

    override fun showTitle(component: Component, seconds: Int) {
        player.showTitle(Title.title(
            component, Component.empty(),
            Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(seconds.toLong()), Duration.ofMillis(250))
        ))
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        player.playSound(location, sound, volume, pitch)
    }

    override fun initHazardBar(hazardType: HazardType, bar: BossBar) {
        check(!hazardBars.containsKey(hazardType)) { "That hazardbar already exists" }
        hazardBars[hazardType] = bar
    }

    override fun setHazardBarProgress(hazardType: HazardType, progress: Float) {
        val bar: BossBar = hazardBars[hazardType]!!
        if (progress == 0f) {
            bar.removeViewer(player)
            return
        }
        bar.addViewer(player)
        bar.progress(progress)
    }

    private fun updateArmor() { // updates all individual armor pieces of the player
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
                0 -> player.inventory.setBoots(null)
                1 -> player.inventory.setLeggings(null)
                2 -> player.inventory.setChestplate(null)
                3 -> player.inventory.setHelmet(null)
            }
        }
    }

    private fun makeArmorPiece(material: Material): ItemStack {
        val armor = ItemStack(material)
        armor.editMeta(LeatherArmorMeta::class.java) { it.setColor(bp.team.dyeColor.color) }
        return armor
    }

    fun spectate(avatar: BotBowsAvatar) {
        player.spectatorTarget = avatar.entity
        player.sendMessage(
            Component.text("Now spectating ", NamedTextColor.GRAY).append(avatar.bp.name)
        )
    }
}
