package gruvexp.bbminigames.menu

import gruvexp.bbminigames.twtClassic.BotBowsPlayer

interface PlayerListMenu {
    fun addPlayer(bp: BotBowsPlayer)
    fun removePlayer(bp: BotBowsPlayer)
    fun updatePlayer(bp: BotBowsPlayer)
}