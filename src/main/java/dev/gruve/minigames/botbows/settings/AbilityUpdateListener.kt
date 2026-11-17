package dev.gruve.minigames.botbows.settings

import dev.gruve.minigames.botbows.BotBowsPlayer
import dev.gruve.minigames.botbows.ability.AbilityType

interface AbilityUpdateListener {
    fun onAbilitiesToggle()
    fun onMaxAbilitiesChange()
    fun onIndividualMaxToggle()
    fun onCooldownMultiplierChange()
    fun onIndividualCooldownToggle()
    fun onUniqueModeToggle()
    fun onUniqueAbilityOccupancyChange(type: AbilityType, bp: BotBowsPlayer, equipped: Boolean)
    fun onRandomizerModeToggle()
    fun onAbilityStatusChange(type: AbilityType)
}