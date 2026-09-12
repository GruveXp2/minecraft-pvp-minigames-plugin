package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.twtClassic.avatar.TeamManager
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.awt.Color
import kotlin.math.min

class BotBowsBoard(val lobby: Lobby) {
    private lateinit var objective: Objective
    var teamManager: TeamManager? = null
        private set

    fun team1(): BotBowsTeam {
        return lobby.settings.team1
    }

    fun team2(): BotBowsTeam {
        return lobby.settings.team2
    }

    fun createBoard(): TeamManager {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objectiveTitle = Component.text("BotBows").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("Classic").color(NamedTextColor.AQUA))
        objective = board.registerNewObjective("botbows", Criteria.DUMMY, objectiveTitle)

        setScore("top_space", Component.text(""), lobby.totalPlayers + 5)
        setScore("team1_score", Component.text("loading..."), 4 + lobby.totalPlayers)
        setScore("team2_score", Component.text("loading..."), 3 + lobby.totalPlayers)
        setScore("separator", Component.text("----------", NamedTextColor.GRAY), lobby.totalPlayers + 2)
        setScore(
            "team1_title",
            Component.text("TEAM ${team1().displayName.uppercase()}", darkenColor(team1().color)),
            lobby.totalPlayers + 1
        )
        setScore(
            "team2_title",
            Component.text("TEAM ${team2().displayName.uppercase()}", darkenColor(team2().color)),
            team2().size()
        )


        for (p in Bukkit.getOnlinePlayers()) {
            p.scoreboard = board
        }
        objective.displaySlot = DisplaySlot.SIDEBAR

        teamManager = TeamManager(board)
        return teamManager!!
    }

    fun initPlayers() {
        for (bp in team1().players) {
            bp.avatar.setColor(team1().color)
        }
        for (bp in team2().players) {
            bp.avatar.setColor(team2().color)
        }
    }

    fun updatePlayerScore(bp: BotBowsPlayer) {
        val hp = bp.hp
        val maxHp = bp.settings.maxHealth
        val playerLineIndex: Int = if (team1().hasPlayer(bp)) {
            team1().getPlayerID(bp) + team2().size() + 1
        } else {
            team2().getPlayerID(bp)
        } // which line of the scoreboard the player stats will be shown
        val healthIcon = if (maxHp > 5) "▏" else "❤"
        val healthBar = Component.text(" ${healthIcon.repeat(hp)}", NamedTextColor.RED)
            .append(Component.text(healthIcon.repeat(maxHp - hp), NamedTextColor.GRAY))
            .append(bp.name)

        setScore(bp.plainName, healthBar, playerLineIndex)
    }

    fun removePlayerScore(bp: BotBowsPlayer) {
        val sb = objective.scoreboard!!
        for (entries in sb.entries) {
            if (bp.plainName in entries) {
                sb.resetScores(entries)
            }
        }
    }

    fun updateTeamScores() {
        updateTeamScore(team1(), "team1_score", 4 + lobby.totalPlayers)
        updateTeamScore(team2(), "team2_score", 3 + lobby.totalPlayers)
    }

    private fun updateTeamScore(team: BotBowsTeam, teamScoreId: String, score: Int) {
        val winThreshold = lobby.settings.winConditionSettings.winScoreThreshold

        val component = Component.text("${team.displayName}: ").append(
            if (winThreshold == 0) {
                Component.text(team1().points, NamedTextColor.WHITE)
            } else if (winThreshold >= 35) {
                Component.text("${team1().points} / ", NamedTextColor.WHITE)
                    .append(Component.text(winThreshold, NamedTextColor.GRAY))
            } else { // use lines with optimized width
                val pointsSystem: String = getPointsSymbol(winThreshold)
                val teamPoints = min(lobby.settings.winConditionSettings.winScoreThreshold, team.points)

                Component.text(pointsSystem.repeat(teamPoints), NamedTextColor.GREEN)
                    .append(Component.text(pointsSystem.repeat(winThreshold - teamPoints), NamedTextColor.GRAY))
            }
        )
        setScore(teamScoreId, component, score)
    }

    private fun setScore(id: String, component: Component?, score: Int) {
        val scoreLine = objective.getScore(id)
        scoreLine.customName(component)
        scoreLine.score = score
    }

    companion object {
        private fun getPointsSymbol(winThreshold: Int): String {
            return if (winThreshold < 8) {
                "█"
            } else if (winThreshold < 9) {
                "▉"
            } else if (winThreshold < 10) {
                "▊"
            } else if (winThreshold < 12) {
                "▋"
            } else if (winThreshold < 15) {
                "▌"
            } else if (winThreshold < 17) {
                "▍"
            } else if (winThreshold < 23) {
                "▎"
            } else if (winThreshold < 34) {
                "▏"
            } else {
                ""
            }
        }

        private fun darkenColor(color: TextColor): TextColor {
            if (color is NamedTextColor) {
                if (color == NamedTextColor.LIGHT_PURPLE) {
                    return NamedTextColor.DARK_PURPLE
                }
                val colorName = "$color"
                val darkened = if (colorName.startsWith("light_")) {
                    NamedTextColor.NAMES.value(colorName.replace("light_", "").lowercase())
                } else {
                    NamedTextColor.NAMES.value("dark_$colorName".lowercase())
                }
                if (darkened != null) return darkened
            }
            val rgb = color.value()
            val red = rgb shr 16 and 0xFF
            val green = rgb shr 8 and 0xFF
            val blue = rgb and 0xFF

            val hsv = Color.RGBtoHSB(red, green, blue, null)

            hsv[1] = min(1.0f, hsv[1] * 1.5f) // saturation
            hsv[2] = hsv[2] * 0.75f // value

            val darkenedRgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2])

            return TextColor.color(darkenedRgb)
        }
    }
}
