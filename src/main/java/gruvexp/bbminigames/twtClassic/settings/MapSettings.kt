package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.map.MapVotingSession

class MapSettings(private val onMapSet: (BotBowsMap) -> Unit, private val onVoteUpdate: (triggeredByNewVote: Boolean) -> Unit)  {
    val mapVotingSession : MapVotingSession = MapVotingSession { notifyVote() }

    var isVoteMode: Boolean = true
        set(value) {
            field = value
            notifyVoteToggle()
        }
    var isWeightedVoting: Boolean = true
        set(value) {
            field = value
            notifyWeightedVotingToggle()
        }
    var currentMap: BotBowsMap = BotBowsMap.RANDOM
        set(value) {
            field = value
            onMapSet(value) // used in Settings to change other menus, like team colors
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
        onVoteUpdate(true)
    }

    private fun notifyVoteToggle() {
        listeners.values.forEach { it.onVoteToggle() }
        onVoteUpdate(false)
    }

    private fun notifyWeightedVotingToggle() {
        listeners.values.forEach { it.onWeightedVotingToggle() }
    }

    private fun notifyMapSet() {
        listeners.values.forEach { it.onMapSet() }
    }

    fun finalizeMapSelection(): BotBowsMap? {
        if (currentMap == BotBowsMap.RANDOM) {
            currentMap = mapVotingSession.classicMapList.random()
            return currentMap
        }
        return null
    }
}