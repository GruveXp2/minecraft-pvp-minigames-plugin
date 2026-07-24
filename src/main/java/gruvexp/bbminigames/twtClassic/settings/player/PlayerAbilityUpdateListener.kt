package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer

interface PlayerAbilityUpdateListener {
    fun onMaxAbilitiesChange(bp: BotBowsPlayer)
    fun onCooldownMultiplierChange(bp: BotBowsPlayer)
}