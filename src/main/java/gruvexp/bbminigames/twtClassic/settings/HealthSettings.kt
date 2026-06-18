package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.settings.player.PlayerSettings

class HealthSettings(val listener: HealthUpdateListener, private val getPlayerSettings: () -> Set<PlayerSettings>) {
    var maxHealth = 3
        set(value) {
            field = value
            listener.onMaxHealthChange()
            if (!isIndividualMaxHealth) getPlayerSettings().forEach { bp -> bp.maxHealth = value }
        }
    var isIndividualMaxHealth = false
        set(value) {
            field = value
            listener.onIndividualMaxHealthToggle()
            if (!isIndividualMaxHealth) getPlayerSettings().forEach { bp -> bp.maxHealth = maxHealth }
        }
    var isCustomDamage = false
        set(value) {
            field = value
            listener.onCustomDamageToggle()
            if (!isCustomDamage) getPlayerSettings().forEach { bp -> bp.attackDamage = 1 }
        }
}