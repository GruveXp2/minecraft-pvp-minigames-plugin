package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.Settings

class PlayerSettings(settings: Settings) {

    var maxHp: Int = settings.maxHP
    var attackDamage: Int = 1
    var maxAbilities: Int = settings.abilitySettings.maxAbilities
    var abilityCooldownMultiplier: Float = settings.abilitySettings.cooldownMultiplier
    var isReady: Boolean = false

}