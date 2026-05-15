package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.map.MapVotingSession

class MapSettings {
    val mapVotingSession : MapVotingSession = MapVotingSession { notifyVote() }

    var isVoteMode: Boolean = true
        set(value) {
            field = value
            notifyVoteToggle()
        }
    var currentMap: BotBowsMap? = null
        set(value) {
            field = value
            notifyMapSet()
        }

    private val listeners = mutableMapOf<BotBowsPlayer, MapUpdateListener>()

    fun addListener(bp: BotBowsPlayer, listener: MapUpdateListener) {
        listeners[bp] = listener
    }

    fun removeListener(bp: BotBowsPlayer) {
        listeners.remove(bp)
    }

    private fun notifyVote() {
        listeners.values.forEach { it.onVote() }
    }

    private fun notifyVoteToggle() {
        listeners.values.forEach { it.onVoteToggle() }
    }

    private fun notifyMapSet() {
        listeners.values.forEach { it.onMapSet() }
    }
}