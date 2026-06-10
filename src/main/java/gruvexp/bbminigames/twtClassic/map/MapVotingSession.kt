package gruvexp.bbminigames.twtClassic.map

import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

data class VoteResult(val maps: Set<BotBowsMap>, val voteCount: Int)

class MapVotingSession(private val onVoteChange: () -> Unit) {
    val votes : MutableMap<BotBowsPlayer, BotBowsMap> = mutableMapOf()
    val classicMapList : Set<BotBowsMap> = BotBowsMap.entries.filter { it.mapType == MapType.CLASSIC && it != BotBowsMap.RANDOM }.toSet()

    fun vote(bp: BotBowsPlayer, map: BotBowsMap) {
        votes[bp] = map
        bp.avatar.message(Component.text("Voted for: ")
            .append(Component.text(map.prettyName(), NamedTextColor.AQUA))
            .append(Component.text(", now "))
            .append(Component.text(getVotes(map), NamedTextColor.GREEN))
            .append(Component.text(" votes")))
        onVoteChange()
    }

    fun removeVote(bp: BotBowsPlayer) {
        votes.remove(bp)
        onVoteChange()
    }

    fun getVotes(map: BotBowsMap): Int {
        return votes.values.count { it == map }
    }

    fun getTotalVotes(): Int {
        return votes.size
    }

    fun getVotedMaps(): Set<BotBowsMap> {
        return votes.values.toSet()
    }

    fun getLeadingMaps(): VoteResult {
        val voteCounts = votes.values // <bb-map, #votes>
            .groupingBy { it }
            .eachCount()

        val maxVotes = voteCounts.values.maxOrNull() ?: 0

        val leadingMaps = voteCounts.filterValues { it == maxVotes }.keys

        return VoteResult(leadingMaps, maxVotes)
    }
}