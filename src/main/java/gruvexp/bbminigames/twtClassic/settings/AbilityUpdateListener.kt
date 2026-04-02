package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType

interface AbilityUpdateListener {
    fun onAbilitiesToggle()
    fun onMaxAbilitiesChange()
    fun onIndividualMaxToggle()
    fun onCooldownMultiplierChange()
    fun onIndividualCooldownToggle()
    fun onUniqueModeToggle()
    fun onUniqueAbilityOccupancyChange(type: AbilityType, bp: BotBowsPlayer, equipped: Boolean)
    fun onAbilityStatusChange(type: AbilityType)
}