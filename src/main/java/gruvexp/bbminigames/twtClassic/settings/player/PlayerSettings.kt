package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings

class PlayerSettings(val bp: BotBowsPlayer, settings: Settings) {

    var maxHealth: Int = settings.healthSettings.maxHealth
        set(value) {
            field = value
            healthListeners.values.forEach { it.onMaxHealthChange(bp) }
            bp.avatar.setMaxHp(value)
        }
    var attackDamage: Int = 1
        set(value) {
            field = value
            healthListeners.values.forEach { it.onAttackDamageChange(bp) }
        }
    var speed: Int = settings.healthSettings.speed
        set(value) {
            field = value
            healthListeners.values.forEach { it.onSpeedChange(bp) }
            bp.avatar.setSpeed(value)
        }
    var maxAbilities: Int = settings.abilitySettings.maxAbilities
        set(value) {
            field = value
            abilityListeners.values.forEach { it.onMaxAbilitiesChange(bp) }
            bp.onMaxAbilitiesChange()
        }

    var abilityCooldownMultiplier: Float = settings.abilitySettings.cooldownMultiplier
        set(value) {
            field = value
            abilityListeners.values.forEach { it.onCooldownMultiplierChange(bp) }
            bp.equippedAbilities.forEach { bp.getAbility(it).cooldownMultiplier = value }
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
}