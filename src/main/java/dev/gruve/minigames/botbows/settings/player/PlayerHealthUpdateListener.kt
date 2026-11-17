package dev.gruve.minigames.botbows.settings.player

import dev.gruve.minigames.botbows.BotBowsPlayer

interface PlayerHealthUpdateListener {
    fun onMaxHealthChange(bp: BotBowsPlayer)
    fun onAttackDamageChange(bp: BotBowsPlayer)
    fun onSpeedChange(bp: BotBowsPlayer)
}