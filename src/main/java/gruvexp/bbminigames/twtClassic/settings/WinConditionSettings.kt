package gruvexp.bbminigames.twtClassic.settings

class WinConditionSettings {
    var isDynamicScoring: Boolean = false // if true, points are awarded based on how many hp left you have, and how many hp from opponent team taken
        set(value) {
            field = value
            listener?.onDynamicScoreToggle()
        }

    var winScoreThreshold: Int = 30 // how much score to win, a score of 0 means no limit
        set(value) {
            field = 0.coerceAtLeast(value)
            listener?.onWinScoreThresholdChange()
        }

    var roundDuration: Int = 5
        set(value) {
            field = 0.coerceAtLeast(value)
            listener?.onRoundDurationChange()
        }

    var listener: WinConditionUpdateListener? = null
}