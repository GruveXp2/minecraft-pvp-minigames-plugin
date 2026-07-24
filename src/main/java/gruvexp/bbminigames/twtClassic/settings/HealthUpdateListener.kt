package gruvexp.bbminigames.twtClassic.settings

interface HealthUpdateListener {
    fun onMaxHealthChange()
    fun onIndividualMaxHealthToggle()
    fun onCustomDamageToggle()
}