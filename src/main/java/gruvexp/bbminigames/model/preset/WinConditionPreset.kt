package gruvexp.bbminigames.model.preset

data class WinConditionPreset(
    val winScoreThreshold: Int,
    val roundDuration: Int,
    val dynamicPoints: Boolean,
)
