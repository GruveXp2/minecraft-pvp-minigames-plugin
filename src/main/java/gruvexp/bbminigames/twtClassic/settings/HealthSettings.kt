package gruvexp.bbminigames.twtClassic.settings

class HealthSettings(val listener: HealthUpdateListener) {
    var maxHealth = 3
        set(value) {
            field = value
            listener.onMaxHealthChange()
        }
    var isIndividualMaxHealth = false
        set(value) {
            field = value
            listener.onIndividualMaxHealthToggle()
        }
    var isCustomDamage = false
        set(value) {
            field = value
            listener.onCustomDamageToggle()
        }
}