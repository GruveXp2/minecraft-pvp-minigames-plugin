package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer

class MapSettings {
    var isVoteMode: Boolean = true
    var currentMap: BotBowsMap? = null

    private val listeners = mutableMapOf<BotBowsPlayer, MapUpdateListener>()

    fun addListener(bp: BotBowsPlayer, listener: MapUpdateListener) {
        listeners[bp] = listener
    }

    fun removeListener(bp: BotBowsPlayer) {
        listeners.remove(bp)
    }
}