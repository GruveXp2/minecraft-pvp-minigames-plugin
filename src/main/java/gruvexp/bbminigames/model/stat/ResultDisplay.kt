package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.team.TeamSide
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.joml.AxisAngle4f


class ResultDisplay(val loc: Location, matchResult: MatchResult) {

    val displays: MutableSet<Display> = mutableSetOf()

    val deathsTab = StatTab(
        "Hits", loc, -1 * HEIGHT_PX, listOf(
            StatCol(
                "hits",
                loc,
                -2.0 * HEIGHT_PX,
                { Component.text(it.hits, NamedTextColor.GREEN) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "dmg",
                loc,
                -2.0 * HEIGHT_PX,
                { Component.text(it.damage, NamedTextColor.RED) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "h/d",
                loc,
                -2.0 * HEIGHT_PX,
                { formatRatio(it.hits, it.damage) },
                matchResult.playerStats.values.toList()
            )
        )
    )

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

    init {
        displays.addAll(listOf(titleBgDisplay, titleDisplay))

        var f = -3 * HEIGHT_PX.toDouble()
        val winningTeam = if( matchResult.team1Won == true) TeamSide.TEAM_1 else TeamSide.TEAM_2
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide == winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, f); f -= HEIGHT_PX }
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide != winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, f); f -= HEIGHT_PX }
    }

    fun formatRatio(positive: Int, negative: Int): TextComponent {
        val ratio = if (negative > 0) positive.toFloat() / negative else positive.toFloat()
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
        return Component.text("%.1f".format(java.util.Locale.US, ratio), TextColor.color(color))
    }

    fun createPlayerRow(bp: BotBowsPlayer, offset: Double) {
        val playerNameDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), TextDisplay::class.java).apply {
            text(bp.name)
            val offset = textWidth(bp.plainName) / 2
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
    }

    fun remove() {
        displays.forEach { it.remove() }
        deathsTab.remove()
    }
}