package gruvexp.bbminigames.model.preset

@JvmRecord
data class WinConditionSettings(
    val winScoreThreshold: Int,
    val roundDuration: Int,
    val dynamicPoints: Boolean,
)
