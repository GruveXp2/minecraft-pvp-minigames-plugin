package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.ability.AbilityType

interface AbilityUpdateListener {
    fun onAbilitiesToggle()
    fun onMaxAbilitiesChange()
    fun onIndividualMaxToggle()
    fun onCooldownMultiplierChange()
    fun onIndividualCooldownToggle()
    fun onAbilityStatusChange(type: AbilityType)
}