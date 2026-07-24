package gruvexp.bbminigames.twtClassic.settings.player

import gruvexp.bbminigames.twtClassic.BotBowsPlayer

interface PlayerHealthUpdateListener {
    fun onMaxHealthChange(bp: BotBowsPlayer)
    fun onAttackDamageChange(bp: BotBowsPlayer)
}