package gruvexp.bbminigames.twtClassic.map

import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

data class VoteResult(val map: BotBowsMap, val voteCount: Int)

class MapVotingSession {
    val votes : MutableMap<BotBowsPlayer, BotBowsMap> = mutableMapOf()
    val classicMapList : Set<BotBowsMap> = BotBowsMap.entries.filter { it.mapType == MapType.CLASSIC }.toSet()

    fun vote(bp: BotBowsPlayer, map: BotBowsMap) {
        votes[bp] = map
        bp.avatar.message(Component.text("Voted for: ")
            .append(Component.text(map.name.lowercase(), NamedTextColor.AQUA))
            .append(Component.text(", now "))
            .append(Component.text(getVotes(map), NamedTextColor.GREEN))
            .append(Component.text(" votes")))
    }

    fun getVotes(map: BotBowsMap): Int {
        return votes.values.count { it == map }
    }

    fun getLeadingMap(): VoteResult {
        val voteCounts = votes.values // <bb-map, #votes>
            .groupingBy { it }
            .eachCount()

        return voteCounts.maxByOrNull { it.value }?.let {
            VoteResult(it.key, it.value)
        } ?: VoteResult(classicMapList.random(), 0)
    }
}