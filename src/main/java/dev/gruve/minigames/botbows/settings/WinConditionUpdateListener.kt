package dev.gruve.minigames.botbows.settings

interface WinConditionUpdateListener {
    fun onDynamicScoreToggle()
    fun onWinScoreThresholdChange()
    fun onRoundDurationChange()
}