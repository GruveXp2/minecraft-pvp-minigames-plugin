package dev.gruve.minigames.menu

import dev.gruve.minigames.botbows.BotBowsPlayer

interface PlayerListMenu {
    fun addPlayer(bp: BotBowsPlayer)
    fun removePlayer(bp: BotBowsPlayer)
    fun updatePlayer(bp: BotBowsPlayer)
}