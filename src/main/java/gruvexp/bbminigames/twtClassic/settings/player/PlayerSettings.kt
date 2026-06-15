package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings

class PlayerSettings(val bp: BotBowsPlayer, settings: Settings) {

    var maxHp: Int = settings.maxHP
        set(value) {
            field = value
            notifyMaxHealthChange()
            bp.avatar.setMaxHP(value)
        }
    var attackDamage: Int = 1
        set(value) {
            field = value
            notifyAttackDamageChange()
        }
    var maxAbilities: Int = settings.abilitySettings.maxAbilities
        set(value) {
            field = value
            notifyMaxAbilitiesChange()
            bp.onMaxAbilitiesChange()
        }

    var abilityCooldownMultiplier: Float = settings.abilitySettings.cooldownMultiplier
        set(value) {
            field = value
            notifyCooldownMultiplierChange()
        }

    var isReady: Boolean = false

    private val abilityListeners = mutableMapOf<BotBowsPlayer, PlayerAbilityUpdateListener>()
    private val healthListeners = mutableMapOf<BotBowsPlayer, PlayerHealthUpdateListener>()

    fun addListener(bp: BotBowsPlayer, healthListener: PlayerHealthUpdateListener, abilityListener: PlayerAbilityUpdateListener) {
        healthListeners[bp] = healthListener
        abilityListeners[bp] = abilityListener
    }

    fun removeListener(bp: BotBowsPlayer) {
        healthListeners.remove(bp)
        abilityListeners.remove(bp)
    }

    fun notifyMaxHealthChange() {
        healthListeners.values.forEach { it.onMaxHpChange(bp) }
    }

    fun notifyAttackDamageChange() {
        healthListeners.values.forEach { it.onAttackDamageChange(bp) }
    }

    fun notifyMaxAbilitiesChange() {
        abilityListeners.values.forEach { it.onMaxAbilitiesChange(bp) }
    }

    fun notifyCooldownMultiplierChange() {
        abilityListeners.values.forEach { it.onCooldownMultiplierChange(bp) }
    }
}