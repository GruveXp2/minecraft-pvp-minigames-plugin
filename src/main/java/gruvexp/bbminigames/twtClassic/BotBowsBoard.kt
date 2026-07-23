package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.twtClassic.avatar.TeamManager
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.awt.Color
import java.util.*
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

    fun createBoard() {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objectiveTitle: Component =
            Component.text("BotBows").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("Classic").color(NamedTextColor.AQUA))
        objective = board.registerNewObjective("botbows", Criteria.DUMMY, objectiveTitle)

        setScore(
            "team2_title",
            Component.text("TEAM " + team2().displayName.uppercase(Locale.getDefault()), darkenColor(team2().color)),
            team2().size()
        )
        setScore(
            "team1_title",
            Component.text("TEAM " + team1().displayName.uppercase(Locale.getDefault()), darkenColor(team1().color)),
            lobby.totalPlayers + 1
        )

        setScore("separator", Component.text("----------", NamedTextColor.GRAY), lobby.getTotalPlayers() + 2)
        setScore("top_space", Component.text(""), lobby.getTotalPlayers() + 5)

        for (p in Bukkit.getOnlinePlayers()) {
            p.scoreboard = board
        }
        objective.displaySlot = DisplaySlot.SIDEBAR

        teamManager = TeamManager(board) // manages scoreboard teams, used for player coloring (username, glow)
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
        removePlayerScore(bp)

        val hp = bp.hp
        val maxHp = bp.settings.maxHealth
        val playerLineIndex: Int = if (team1().hasPlayer(bp)) {
            team1().getPlayerID(bp) + team2().size() + 1
        } else {
            team2().getPlayerID(bp)
        } // which line of the scoreboard the player stats will be shown
        val healthBar = if (maxHp > 5) {
            ChatColor.RED.toString() + "▏".repeat(hp) + ChatColor.GRAY + "▏".repeat(maxHp - hp) + toChatColor(bp.getTeamColor() as NamedTextColor?) + " " + bp.getPlainName()
        } else {
            ChatColor.RED.toString() + "❤".repeat(hp) + ChatColor.GRAY + "❤".repeat(maxHp - hp) + toChatColor(bp.getTeamColor() as NamedTextColor?) + " " + bp.getPlainName()
        }

        setScore(healthBar, playerLineIndex)
    }

    fun removePlayerScore(bp: BotBowsPlayer) {
        val sb = objective.scoreboard!!
        for (entries in sb.entries) {
            if (entries.contains(bp.plainName)) {
                sb.resetScores(entries)
            }
        }
    }

    fun updateTeamScores() {
        val sb = objective.scoreboard!!
        val winThreshold = lobby.settings.winConditionSettings.winScoreThreshold

        for (entries in sb.entries) {
            if (entries.contains(team1().displayName + ": ")) {
                sb.resetScores(entries)
            }
            if (entries.contains(team2().displayName + ": ")) {
                sb.resetScores(entries)
            }
        }
        val totalPlayers = lobby.totalPlayers
        if (winThreshold == 0) {
            setScore(
                toChatColor(team1().color).toString() + team1().displayName + ": " + ChatColor.RESET + team1().points,
                4 + totalPlayers
            ) // legger inn scoren til hvert team
            setScore(
                toChatColor(team2().color).toString() + team2().displayName + ": " + ChatColor.RESET + team2().points,
                3 + totalPlayers
            )
        } else if (winThreshold >= 35) {
            setScore(
                toChatColor(team1().color).toString() + team1().displayName + ": " + ChatColor.RESET + team1().points + " / " + ChatColor.GRAY + winThreshold,
                4 + totalPlayers
            ) // legger inn scoren til hvert team
            setScore(
                toChatColor(team2().color).toString() + team2().displayName + ": " + ChatColor.RESET + team2().points + " / " + ChatColor.GRAY + winThreshold,
                3 + totalPlayers
            )
        } else { // få plass til mest mulig streker
            val healthSymbol: String = getHealthSymbol(winThreshold)
            val team1Points = min(lobby.settings.winConditionSettings.winScoreThreshold, team1().points)
            val team2Points = min(lobby.settings.winConditionSettings.winScoreThreshold, team2().points)

            setScore(
                toChatColor(team1().color).toString() + team1().displayName + ": " + ChatColor.GREEN + healthSymbol.repeat(
                    team1Points
                ) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team1Points), 4 + totalPlayers
            ) // legger inn scoren til hvert team
            setScore(
                toChatColor(team2().color).toString() + team2().displayName + ": " + ChatColor.GREEN + healthSymbol.repeat(
                    team2Points
                ) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team2Points), 3 + totalPlayers
            )
        }
    }

    private fun setScore(text: String, score: Int) {
        val l1 = objective.getScore(text)
        l1.score = score
    }

    private fun setScore(id: String, component: Component?, score: Int) {
        val scoreLine = objective.getScore(id)
        scoreLine.customName(component)
        scoreLine.score = score
    }

    fun test() {
        val test = objective.getScore("test")
        test.score = 10
        test.customName(Component.text("custom colors letsgooo", TextColor.color(124, 15, 76)))
    }

    companion object {
        private fun getHealthSymbol(winThreshold: Int): String {
            var c = ""
            if (winThreshold < 8) {
                c = "█"
            } else if (winThreshold < 9) {
                c = "▉"
            } else if (winThreshold < 10) {
                c = "▊"
            } else if (winThreshold < 12) {
                c = "▋"
            } else if (winThreshold < 15) {
                c = "▌"
            } else if (winThreshold < 17) {
                c = "▍"
            } else if (winThreshold < 23) {
                c = "▎"
            } else if (winThreshold < 34) {
                c = "▏"
            }
            return c
        }

        private fun darkenColor(color: TextColor): TextColor {
            if (color is NamedTextColor) {
                if (color === NamedTextColor.LIGHT_PURPLE) {
                    return NamedTextColor.DARK_PURPLE
                }
                val colorName = color.toString()
                val darkened: NamedTextColor?
                if (colorName.startsWith("light_")) {
                    darkened =
                        NamedTextColor.NAMES.value(colorName.replace("light_", "").lowercase(Locale.getDefault()))
                } else {
                    darkened = NamedTextColor.NAMES.value(("dark_$colorName").lowercase(Locale.getDefault()))
                }
                if (darkened != null) return darkened
            }
            val rgb = color.value()
            val red = (rgb shr 16) and 0xFF
            val green = (rgb shr 8) and 0xFF
            val blue = rgb and 0xFF

            val hsv = Color.RGBtoHSB(red, green, blue, null)

            hsv[1] = min(1.0f, hsv[1] * 1.5f) // saturation
            hsv[2] = hsv[2] * 0.75f // value

            val darkenedRgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2])

            return TextColor.color(darkenedRgb)
        }

        private fun toChatColor(textColor: TextColor?): ChatColor {
            if (textColor === NamedTextColor.RED) return ChatColor.RED
            if (textColor === NamedTextColor.BLUE) return ChatColor.BLUE
            if (textColor === NamedTextColor.GREEN) return ChatColor.GREEN
            if (textColor === NamedTextColor.YELLOW) return ChatColor.YELLOW
            if (textColor === NamedTextColor.WHITE) return ChatColor.WHITE
            if (textColor === NamedTextColor.BLACK) return ChatColor.BLACK
            if (textColor === NamedTextColor.GRAY) return ChatColor.GRAY
            if (textColor === NamedTextColor.DARK_GRAY) return ChatColor.DARK_GRAY
            if (textColor === NamedTextColor.DARK_RED) return ChatColor.DARK_RED
            if (textColor === NamedTextColor.DARK_BLUE) return ChatColor.DARK_BLUE
            if (textColor === NamedTextColor.DARK_GREEN) return ChatColor.DARK_GREEN
            if (textColor === NamedTextColor.DARK_AQUA) return ChatColor.DARK_AQUA
            if (textColor === NamedTextColor.DARK_PURPLE) return ChatColor.DARK_PURPLE
            if (textColor === NamedTextColor.GOLD) return ChatColor.GOLD
            if (textColor === NamedTextColor.AQUA) return ChatColor.AQUA
            if (textColor === NamedTextColor.LIGHT_PURPLE) return ChatColor.LIGHT_PURPLE
            return ChatColor.WHITE
        }
    }
}
