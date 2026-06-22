package gruvexp.bbminigames.twtClassic.settings

class WinConditionSettings {
    var isDynamicScoring: Boolean = false // if true, points are awarded based on how many hp left you have, and how many hp from opponent team taken
        set(value) {
            field = value
            notifyDynamicScoreToggle()
        }

    var winScoreThreshold: Int = 30 // how much score to win, a score of 0 means no limit
        set(value) {
            field = 0.coerceAtLeast(value)
            notifyWinScoreThresholdChange()
        }

    var roundDuration: Int = 5
        set(value) {
            field = 0.coerceAtLeast(value)
            notifyRoundDurationChange()
        }

    var listener: WinConditionUpdateListener? = null

    fun notifyDynamicScoreToggle() {
        listener?.onDynamicScoreToggle()
    }

    fun notifyWinScoreThresholdChange() {
        listener?.onWinScoreThresholdChange()
    }

    fun notifyRoundDurationChange() {
        listener?.onRoundDurationChange()
    }
}