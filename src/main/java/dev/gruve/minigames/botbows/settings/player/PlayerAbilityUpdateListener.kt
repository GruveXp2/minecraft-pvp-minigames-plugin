package dev.gruve.minigames.botbows.settings.player

import dev.gruve.minigames.botbows.BotBowsPlayer

interface PlayerAbilityUpdateListener {
    fun onMaxAbilitiesChange(bp: BotBowsPlayer)
    fun onCooldownMultiplierChange(bp: BotBowsPlayer)
}