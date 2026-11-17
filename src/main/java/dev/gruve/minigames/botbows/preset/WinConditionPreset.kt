package dev.gruve.minigames.botbows.preset

data class WinConditionPreset(
    val winScoreThreshold: Int,
    val roundDuration: Int,
    val dynamicPoints: Boolean,
)
