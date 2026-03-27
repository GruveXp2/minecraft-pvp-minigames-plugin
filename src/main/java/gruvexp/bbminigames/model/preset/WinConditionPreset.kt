package gruvexp.bbminigames.model.preset

@JvmRecord
data class WinConditionPreset(
    val winScoreThreshold: Int,
    val roundDuration: Int,
    val dynamicPoints: Boolean,
)
