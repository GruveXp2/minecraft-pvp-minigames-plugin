package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer

interface PlayerHealthUpdateListener {
    fun onMaxHpChange(bp: BotBowsPlayer)
    fun onAttackDamageChange(bp: BotBowsPlayer)
}