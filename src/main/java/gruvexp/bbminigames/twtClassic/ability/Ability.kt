package gruvexp.bbminigames.twtClassic.ability

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.abilities.*
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

open class Ability(@JvmField protected val bp: BotBowsPlayer, val hotBarSlot: Int, @JvmField val type: AbilityType) {
    @JvmField
    protected var baseCooldown: Int = 0 // seconds
    var cooldownMultiplier: Float = 1.0f
        set(value) {
            field = value
            effectiveCooldown = (baseCooldown * value).toInt()
        }
    var effectiveCooldown: Int = 0
        private set

    private var cooldownTimer: CooldownTimer? = null
    var cooldownTickRate = 20
        set(value) {
            field = value
            cooldownTimer?.let {
                it.cancel()
                cooldownTimer = CooldownTimer(bp, it.currentCooldown).apply {
                    runTaskTimer(Main.getPlugin(), 0L, value.toLong())
                }
            }
        }

    init {
        if (type.category != AbilityCategory.DAMAGING) {
            this.baseCooldown = type.baseCooldown
        }
    }

    fun resetCooldown() {
        cooldownTimer?.cancel()
        cooldownTimer = null
    }

    fun obtain() {
        resetCooldown()
        bp.avatar.setItem(hotBarSlot, type.abilityItem)
    }

    fun lose() {
        if (type.category == AbilityCategory.DAMAGING) {
            bp.avatar.setItem(hotBarSlot, type.cooldownItems[0].clone())
        } else {
            cooldownTimer = CooldownTimer(bp, effectiveCooldown).apply {
                runTaskTimer(Main.getPlugin(), 0L, cooldownTickRate.toLong())
            }
        }
    }

    fun hit() {
        cooldownTimer?.hit()
    }

    open fun use() {
        val game = bp.lobby.botBowsGame ?: return
        if (game.canMove) return  // null check used when testing ability outside of match

        if (type.category == AbilityCategory.DAMAGING) {
            bp.loseWeaponAbilities()
        } else {
            cooldownTimer = CooldownTimer(bp, effectiveCooldown).apply {
                runTaskTimer(Main.getPlugin(), 0L, cooldownTickRate.toLong())
            }
        }
    }

    fun registerSuccess() = bp.lobby.botBowsGame!!.matchResult.registerAbilitySuccess(bp, type)
    fun registerFail() = bp.lobby.botBowsGame!!.matchResult.registerAbilityFail(bp, type)

    private fun getCooldownItem(cooldown: Int) = when {
        cooldown > 10 -> type.cooldownItems[0].clone()
        cooldown > 5 ->  type.cooldownItems[1].clone()
        cooldown > 2 ->  type.cooldownItems[2].clone()
        else ->          type.cooldownItems[3].clone()
    }

    open fun unequip() {
    }

    open fun reset() { // removing things gracefully (eg igniting creeper, remove effects etc)
    }

    open fun destroy() { // removing everything by force (removing all entities without any effect)
        resetCooldown()
    }

    private inner class CooldownTimer(private val bp: BotBowsPlayer, var currentCooldown: Int) : BukkitRunnable() {
        var cooldownItem: ItemStack = getCooldownItem(currentCooldown)

        override fun run() {
            if (currentCooldown <= 0) {
                obtain()
                return
            }

            when (currentCooldown) {
                10 -> cooldownItem = type.cooldownItems[1].clone()
                5 ->  cooldownItem = type.cooldownItems[2].clone()
                2 ->  cooldownItem = type.cooldownItems[3].clone()
            }
            cooldownItem.amount = currentCooldown
            bp.avatar.setItem(hotBarSlot, cooldownItem)
            currentCooldown--
        }

        fun hit() { // when someone hits you with a bow, the cooldown wont go down until your invulnerability period is over
            currentCooldown += BotBows.HIT_DISABLED_ITEM_TICKS / 20
        }
    }

    companion object {
        fun create(type: AbilityType, bp: BotBowsPlayer, slot: Int): Ability = when (type) {
            AbilityType.ENDER_PEARL -> object : Ability(bp, slot, AbilityType.ENDER_PEARL) {
                override fun use() = registerSuccess()
            }
            AbilityType.RADAR -> Radar(bp, slot)
            AbilityType.SPLASH_BOW -> SplashBow(bp, slot)
            AbilityType.THUNDER_BOW -> ThunderBow(bp, slot)
            AbilityType.LONG_ARMS -> LongArms(bp, slot)
            AbilityType.SALMON_SLAP -> SalmonSlap(bp, slot)
            AbilityType.BUBBLE_JET -> BubbleJet(bp, slot)
            AbilityType.CREEPER_TRAP -> CreeperTrap(bp, slot)
            AbilityType.BABY_POTION -> BabyPotion(bp, slot)
            AbilityType.LINGERING_POTION -> LingeringPotionTrap(bp, slot)
            AbilityType.CHARGE_POTION -> ChargePotion(bp, slot)
            AbilityType.KARMA_POTION -> KarmaPotion(bp, slot)
            AbilityType.LASER_TRAP -> LaserTrap(bp, slot)
        }
    }
}
