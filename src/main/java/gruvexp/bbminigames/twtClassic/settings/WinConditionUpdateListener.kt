package gruvexp.bbminigames.twtClassic.settings

interface WinConditionUpdateListener {
    fun onDynamicScoreToggle()
    fun onWinScoreThresholdChange()
    fun onRoundDurationChange()
}