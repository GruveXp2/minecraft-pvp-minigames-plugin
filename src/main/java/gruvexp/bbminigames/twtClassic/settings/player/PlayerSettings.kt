package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings

class PlayerSettings(val bp: BotBowsPlayer, settings: Settings) {

    var maxHp: Int = settings.maxHP
    var attackDamage: Int = 1
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

    fun addListener(bp: BotBowsPlayer, listener: PlayerAbilityUpdateListener) {
        abilityListeners[bp] = listener
    }

    fun removeListener(bp: BotBowsPlayer) {
        abilityListeners.remove(bp)
    }

    fun notifyMaxAbilitiesChange() {
        abilityListeners.values.forEach { it.onMaxAbilitiesChange(bp) }
    }

    fun notifyCooldownMultiplierChange() {
        abilityListeners.values.forEach { it.onCooldownMultiplierChange(bp) }
    }
}