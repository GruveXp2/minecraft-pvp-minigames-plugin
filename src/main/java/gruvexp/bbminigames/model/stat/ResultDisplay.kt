package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.team.TeamSide
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.joml.AxisAngle4f

const val PX = 0.025f
const val X = -PX/2f // Workaround to undo mojangs hardcoded bug that offsets text for no reason (textshadow that isnt there)

class ResultDisplay(val loc: Location, matchResult: MatchResult) {

    val displays: MutableSet<Display> = mutableSetOf()

    val titleBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply { scale.set(24f, 1f, 1f); translation.set(X*24, 0f, 0f) }

        billboard = Display.Billboard.VERTICAL
    }

    val titleDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text("MATCH RESULTS"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val headerBgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(24f, 1f, 1f); translation.set(X*24, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }

    val killsHeaderDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("kills"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X -1f, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val deathsHeaderDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("deaths"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val kdRatioHeaderDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("k/d"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(X+ 1, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    init {
        displays.addAll(listOf(titleBgDisplay, titleDisplay, headerBgDisplay, killsHeaderDisplay, deathsHeaderDisplay, kdRatioHeaderDisplay))

        var f = -0.625
        val winningTeam = if( matchResult.team1Won == true) TeamSide.TEAM_1 else TeamSide.TEAM_2
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide == winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, stats, f); f -= 0.25 }
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide != winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, stats, f); f -= 0.25 }
    }

    fun createPlayerRow(bp: BotBowsPlayer, stats: PlayerMatchStats, offset: Double) {
        val playerNameDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), TextDisplay::class.java).apply {
            text(bp.name)
            val offset = calculateTextWidth(bp.plainName) / 2
            transformation = transformation.apply { translation.set(X -1.5f - 6*PX - offset, 0f, 0.01f); }
            billboard = Display.Billboard.VERTICAL
        }

        val playerHeadDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), ItemDisplay::class.java).apply {
            setItemStack(bp.avatar.headItem)
            transformation = transformation.apply {
                translation.set(-1.4f, 8*PX, 0.01f)
                scale.set(0.25f, 0.25f, 0.25f)
                leftRotation.set(AxisAngle4f(Math.toRadians(180.0).toFloat(), 0f, 1f, 0f))
            }
            billboard = Display.Billboard.VERTICAL
        }

        val playerStatsBgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), TextDisplay::class.java).apply {
            text(Component.text(" "))
            val teamColor = bp.teamColor
            backgroundColor = Color.fromARGB(100, teamColor.red(), teamColor.green(), teamColor.blue())
            transformation = transformation.apply {
                translation.set(24*X, 0f, 0f)
                scale.set(24f, 1f, 1f)
            }
            billboard = Display.Billboard.VERTICAL
        }

        displays.addAll(listOf(playerNameDisplay, playerStatsBgDisplay, playerHeadDisplay))

        createPlayerStatDisplay(offset, -1f, Component.text(stats.kills, TextColor.color(0x88FF88)), 2.5f)
        createPlayerStatDisplay(offset, 0f, Component.text(stats.deaths, TextColor.color(0xFF8888)), 2.5f)

        val ratio = if (stats.deaths > 0) stats.kills.toFloat() / stats.deaths else stats.kills.toFloat()
        val color: Int = when {
            ratio > 5.0 -> 0x40FF40
            ratio > 2.0 -> 0x80FF80
            ratio > 1.3 -> 0xC0FFC0
            ratio > 1.0 -> 0xE0FFD0
            ratio > 0.8 -> 0xFFE0D0
            ratio > 0.5 -> 0xFFC0C0
            ratio > 0.2 -> 0xFF8080
            else -> 0xFF4040
        }
        val text = Component.text("%.1f".format(java.util.Locale.US, ratio), TextColor.color(color))
        createPlayerStatDisplay(offset, 1f, text, 4f)
    }

    fun createPlayerStatDisplay(yOffset: Double, xOffset: Float, text: TextComponent, bgWidth: Float) {
        val bgDisplay = Main.WORLD.spawn(loc.clone().add(0.0, yOffset, 0.0), TextDisplay::class.java).apply {
            text(Component.text(" "))
            transformation = transformation.apply {
                scale.set(bgWidth, 1f, 1f)
                translation.set(X*bgWidth+ xOffset, 0f, 0.01f)
            }
            billboard = Display.Billboard.VERTICAL
        }

        val statDisplay = Main.WORLD.spawn(loc.clone().add(0.0, yOffset, 0.0), TextDisplay::class.java).apply {
            text(text)
            backgroundColor = Color.fromARGB(0)
            transformation = transformation.apply { translation.set(X+ xOffset, 0f, 0.02f) }
            billboard = Display.Billboard.VERTICAL
        }
        displays.addAll(listOf(bgDisplay, statDisplay))
    }

    fun remove() {
        displays.forEach { it.remove() }
    }

    fun calculateTextWidth(text: String, scale: Float = 1f): Float {
        var width = 1
        for (char in text) {
            width += when (char) {
                'i', '!', '|', '\'', '.', ',', ':', ';' -> 2
                'l' -> 3
                'I', '(', ')', '{', '}', '[', ']', 't', '"', ' ' -> 4
                'f', 'k', '<', '>' -> 5
                '@', '~' -> 7
                'æ', 'Æ' -> 10
                else -> 6
            }
        }
        return width * PX * scale
    }
}