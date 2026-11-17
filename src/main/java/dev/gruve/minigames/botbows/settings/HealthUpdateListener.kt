package dev.gruve.minigames.botbows.settings

interface HealthUpdateListener {
    fun onMaxHealthChange()
    fun onIndividualMaxHealthToggle()
    fun onCustomDamageToggle()
    fun onSpeedChange()
    fun onIndividualSpeedToggle()
}