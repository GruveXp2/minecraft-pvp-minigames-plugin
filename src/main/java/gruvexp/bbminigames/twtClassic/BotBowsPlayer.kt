package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.menu.menus.AbilityMenu
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.Ability.Companion.create
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.abilities.KarmaPotion
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar
import gruvexp.bbminigames.twtClassic.avatar.NpcAvatar
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar
import gruvexp.bbminigames.twtClassic.avatar.TeamManager
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import gruvexp.bbminigames.twtClassic.settings.player.PlayerSettings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class BotBowsPlayer {
    @JvmField
    var avatar: BotBowsAvatar

    @JvmField
    val lobby: Lobby
    @JvmField
    val settings: PlayerSettings
    var team: BotBowsTeam? = null
        private set
    var hp: Int = 3
        private set(value) {
            field = value
            avatar.setHP(value)
            if (value == 0) avatar.eliminate()
            lobby.botBowsGame!!.botBowsBoard.updatePlayerScore(this)
        }
    var isDamaged: Boolean = false // cooldown når playeren er hitta
        private set
    private var sneakManager: SneakManager? = null
    val effectManager: PlayerEffectManager

    var karmaAura = false

    private val abilities: MutableMap<AbilityType, Ability> = mutableMapOf()
    val equippedAbilities: Set<AbilityType>
        get() = abilities.keys
    var usedAbilityItemAmount: Int = 0
        private set
    var hasKarmaEffect = false

    constructor(player: Player, lobbySettings: Settings) {
        avatar = PlayerAvatar(player, this)
        plainName = player.name
        settings = PlayerSettings(this, lobbySettings)
        lobby = lobbySettings.lobby
        hp = settings.maxHealth
        effectManager = PlayerEffectManager(this)
    }

    constructor(mannequin: Mannequin, lobbySettings: Settings) {
        avatar = NpcAvatar(mannequin, this)
        plainName = mannequin.name
        settings = PlayerSettings(this, lobbySettings)
        lobby = lobbySettings.lobby
        hp = settings.maxHealth
        effectManager = PlayerEffectManager(this)
        setReady(true, 4) // bots are always ready for match
    }

    val teamColor: TextColor
        get() = team?.color ?: NamedTextColor.WHITE

    val name: Component
        get() = Component.text(plainName, teamColor)

    val plainName: String

    fun onTeamJoin(team: BotBowsTeam) {
        this.team?.leave(this)
        this.team = team
        avatar.equipFullArmor()
    }

    fun updateTeam(team: BotBowsTeam) {
        this.team = team
    }

    fun onTeamLeave() {
        team = null
    }

    fun onGameLeave() {
        team!!.leave(this)
        avatar.destroy()
        effectManager.clear()
        abilities.keys.forEach { unequipAbility(it, true) }
    }

    fun turnIntoBot(): UUID {
        check(avatar is NpcAvatar) { "This botbowsplayer is already a bot!" }

        val bot = Main.WORLD.spawn(avatar.getEntity().location, Mannequin::class.java)
        bot.customName(name)
        bot.profile = ResolvableProfile.resolvableProfile(Bukkit.createProfile(avatar.getUUID()))
        avatar = NpcAvatar(bot, avatar)
        avatar.setHP(hp)
        avatar.readyBattle(lobby.botBowsGame!!.botBowsBoard.teamManager) // this line is kinda ugly, maybe make the teammanager be somewhere else idk
        return bot.uniqueId
    }

    fun turnIntoPlayer(p: Player) {
        check(avatar is PlayerAvatar) { "This botbowsplayer is already a player!" }

        p.teleport(avatar.location)
        avatar.destroy()
        avatar = PlayerAvatar(p, avatar)
        avatar.setMaxHP(settings.maxHealth)
        avatar.setHP(hp)
    }

    fun destroy() {
        avatar.destroy()
        effectManager.clear()
        sneakManager?.destroy()
        abilities.values.forEach { it.destroy() }
    }

    fun start() {
        sneakManager = SneakManager(avatar)
    }

    fun revive() { // resetter for å gjør klar til en ny runde
        hp = settings.maxHealth
        avatar.revive()
        isDamaged = false
    }

    fun reset() {
        sneakManager?.destroy()
        avatar.reset()
        hasKarmaEffect = false
    }

    fun initBattle(teamManager: TeamManager) {
        avatar.readyBattle(teamManager)
        abilities.values.forEach { it.cooldownMultiplier = settings.abilityCooldownMultiplier }
    }

    fun clearEffects() {
        effectManager.clear()
    }

    fun readyAbilities() {
        abilities.values.forEach { it.obtain() }
    }

    fun resetAbilities() {
        abilities.values.forEach { it.reset() }
    }

    fun registerUsedAbilityItem(abilityItemAmount: Int) {
        this.usedAbilityItemAmount = abilityItemAmount
    }

    val isAlive: Boolean
        get() = hp > 0

    private val abilityMenu: AbilityMenu?
        get() = lobby.settings.abilityMenus[this]

    fun getAbility(type: AbilityType): Ability {
        return abilities[type]!!
    }

    fun onMaxAbilitiesChange() {
        val maxAbilities = settings.maxAbilities
        if (this.totalAbilities <= maxAbilities) return
        val excess = this.totalAbilities - maxAbilities
        repeat(excess) {
            for (type in AbilityType.entries) {
                if (!hasAbilityEquipped(type)) continue
                unequipAbility(type)
                break
            }
        }
    }

    fun equipAbility(type: AbilityType) {
        val slot = avatar.getNextFreeSlot()
        equipAbility(slot, type)
    }

    @JvmOverloads
    fun equipAbility(slot: Int, type: AbilityType, updateInventory: Boolean = true) {
        if (lobby.settings.abilitySettings.maxAbilities == 0) return
        val abilityAlreadyEquipped = hasAbilityEquipped(type)
        if (!abilityAlreadyEquipped) {
            val result = lobby.settings.abilitySettings.attemptEquip(this, type)
            if (!result) {
                avatar.message(Component.text(
                    "Cant equip, ability already in use by team member (unique ability mode enabled)",
                    NamedTextColor.YELLOW
                ))
                return
            }
        }
        abilities[type] = create(type, this, slot)
        if (abilityAlreadyEquipped) return

        if (slot > 0 && updateInventory) {
            avatar.setItem(slot, type.getAbilityItem(this))
        }

        if (type == AbilityType.BUBBLE_JET) lobby.settings.rain++

        avatar.message(
            Component.text("Equipping ability: ", NamedTextColor.GREEN)
                .append(Component.text(type.displayName, NamedTextColor.LIGHT_PURPLE))
        )
        this.abilityMenu?.onAbilityStatusChange(type)
    }

    @JvmOverloads
    fun unequipAbility(type: AbilityType, hideMessage: Boolean = false) {
        if (!abilities.containsKey(type)) return

        lobby.settings.abilitySettings.unequip(this, type)
        val ability: Ability = abilities[type]!!
        ability.resetCooldown()
        ability.unequip()
        val slot = ability.hotBarSlot
        if (slot > 0) {
            avatar.setItem(slot, null)
        }

        abilities.remove(type)
        if (type == AbilityType.BUBBLE_JET) lobby.settings.rain--

        if (!hideMessage) {
            avatar.message(
                Component.text("Unequipping ability: ", NamedTextColor.RED)
                    .append(Component.text(type.displayName, NamedTextColor.LIGHT_PURPLE))
            )
        }
        this.abilityMenu?.onAbilityStatusChange(type)
    }

    fun hasAbilityEquipped(type: AbilityType?): Boolean {
        return abilities.containsKey(type)
    }

    fun obtainWeaponAbilities() {
        abilities.values.filter { it.type.category == AbilityCategory.DAMAGING }
            .forEach { it.obtain() }
    }

    fun loseWeaponAbilities() {
        abilities.values.filter { it.type.category == AbilityCategory.DAMAGING }
            .forEach { it.lose() }
    }

    val totalAbilities: Int
        get() = abilities.size

    fun damage(ctx: DamageContext) {
        if (isDamaged || !this.isAlive) return
        avatar.damage()
        var damageMessage = ctx.formatMessage(this)

        val isFatal = ctx is DamageContext.Player && hp <= ctx.attacker.settings.attackDamage

        if (ctx is DamageContext.Environment) {
            die(damageMessage)
        } else if (ctx is DamageContext.Player) {
            if (this !== ctx.attacker) { // dont register hit/dmg if it was self inflicted
                lobby.botBowsGame!!.matchResult.registerDamage(this)
                lobby.botBowsGame!!.matchResult.registerHit(ctx.attacker)
            }
            if (isFatal) {
                lobby.botBowsGame!!.matchResult.registerDeath(this)
                lobby.botBowsGame!!.matchResult.registerKill(ctx.attacker)
                damageMessage = damageMessage
                    .append(Component.text(" and got"))
                    .append(Component.text(" eliminated", NamedTextColor.DARK_RED))
                die(damageMessage)
                return
            }
            hp -= ctx.attacker.settings.attackDamage
            lobby.messagePlayers(ctx.formatMessage(this))
            abilities.values.forEach { it.hit() } // pauses the cooldowns etc
            isDamaged = true
            Bukkit.getScheduler().runTaskLater(
                Main.getPlugin(),
                Runnable { isDamaged = false },
                BotBows.HIT_DISABLED_ITEM_TICKS.toLong()
            )
        }
    }

    private fun die(deathMessage: Component) {
        this.hp = 0
        lobby.botBowsGame!!.botBowsBoard.updatePlayerScore(this)
        lobby.messagePlayers(deathMessage)
        abilities.values.forEach { it.cooldownTickRate = 20 }
        hasKarmaEffect = false
        lobby.check4Elimination(this)
    }

    fun setReady(ready: Boolean, itemIndex: Int) {
        if (settings.isReady == ready) return  //TODO: skal inn i listneren og

        settings.isReady = ready // todo, flytt inn i playersettings det med itemindex. det må og med tror jeg
        avatar.setReady(ready, itemIndex) // listener og ikke her. playersettings skal ha full ctrl
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { lobby.handlePlayerReady(this) }, 3L)
    }

    fun setAbilityCooldownTickRate(abilityCooldownTickRate: Int) {
        for (ability in abilities.values) {
            if (ability.type == AbilityType.CHARGE_POTION) continue  // charge potion wont affect itself

            ability.cooldownTickRate = abilityCooldownTickRate
        }
    }

    fun applyKarmaDebuff() {
        val effects = arrayOf(
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.BLINDNESS,
            PotionEffectType.UNLUCK
        )
        effectManager.applyGlow(
            PlayerEffectManager.GlowSource.DEBUFF,
            (KarmaPotion.KARMA_DURATION * 20).toLong(),
            NamedTextColor.GOLD,
            10
        )

        val randomEffect = effects.random()
        if (randomEffect == PotionEffectType.UNLUCK) {
            effectManager.applyScale(
                PlayerEffectManager.ScaleSource.GROW_KARMA,
                1.5,
                PlayerEffectManager.ScalePriority.NORMAL,
                (KarmaPotion.KARMA_DURATION * 20).toLong()
            )
            return
        }

        avatar.addPotionEffect(PotionEffect(randomEffect, KarmaPotion.KARMA_DURATION * 20, 1))

        lobby.messagePlayers(
            Component.empty()
                .append(name)
                .append(Component.text(" got karma! ", NamedTextColor.RED))
                .append(Component.text(randomEffect.key.value(), NamedTextColor.DARK_RED))
        )
    }

    val isSneakingExhausted: Boolean
        get() = sneakManager!!.isSneakingExhausted

    fun reloadBotBow() {
        avatar.setItem(0, BotBows.BOTBOW)
    }

    fun getNearbyPlayers(radius: Double): Set<BotBowsPlayer> {
        return avatar.location.world.getNearbyEntities(avatar.location, radius, radius, radius)
            .mapNotNull { BotBows.getBotBowsPlayer(it.uniqueId) }
            .filter { it.isAlive }
            .toSet()
    }

    val location: Location?
        get() = avatar.location

    fun teleport(location: Location) {
        avatar.teleport(location)
    }

    fun setInvulnerable(invulnerable: Boolean) {
        avatar.setInvulnerable(invulnerable)
    }

    val isOnGround: Boolean
        get() = avatar.isOnGround

    companion object {
        @JvmField
        val HEALTH_ARMOR: List<List<Set<Int>>> = listOf(
            listOf( // maxHp = 2
                setOf(0, 1, 2, 3)
            ),
            listOf( // maxHp = 3
                setOf(0, 2),
                setOf(1, 3)
            ),
            listOf( // maxHp = 4
                setOf(2),
                setOf(0, 1),
                setOf(3)
            ),
            listOf( // maxHp = 5
                setOf(2),
                setOf(1),
                setOf(0),
                setOf(3),
                setOf() // hvis man har fler liv enn 5 så blir denne calla, men da skal det ikke skje noe
            )
        ) // Når man tar damag så kan man gette em liste med hvilke armor pieces som skal fjernes
    }
}
