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
    val tabs: MutableSet<StatTab> = mutableSetOf()

    val hitsTab = StatTab("Hits", loc, -1 * HEIGHT_PX) { recalculateTabs() }.also { tab ->
        tab.addColumns(listOf(
            StatCol(
                "hits",
                loc,
                tab,
                -1f * HEIGHT_PX,
                { Component.text(it.hits, NamedTextColor.GREEN) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "dmg",
                loc,
                tab,
                -1f * HEIGHT_PX,
                { Component.text(it.damage, NamedTextColor.RED) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "h/d",
                loc,
                tab,
                -1f * HEIGHT_PX,
                { formatRatio(it.hits, it.damage) },
                matchResult.playerStats.values.toList()
            )
        ))
        tabs.add(tab)
    }

    val deathsTab = StatTab("Deaths", loc, -HEIGHT_PX) { recalculateTabs() }.also { tab ->
        tab.addColumns(listOf(
            StatCol(
                "survival",
                loc,
                tab,
                -HEIGHT_PX,
                { formatPercentage(matchResult.rounds, matchResult.rounds - it.deaths) },
                matchResult.playerStats.values.toList()
            )
        ))
        tab.addHiddenColumns(listOf(
            StatCol(
                "deaths",
                loc,
                tab,
                -HEIGHT_PX,
                { Component.text(it.deaths, NamedTextColor.RED) },
                matchResult.playerStats.values.toList()
            )
        ))
        tabs.add(tab)
    }

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

    val team1BgDisplay: TextDisplay
    val team2BgDisplay: TextDisplay

    init {
        displays.addAll(listOf(titleBgDisplay, titleDisplay))

        var f = -3 * HEIGHT_PX.toDouble()
        val winningTeam = if (matchResult.team1Won == true) TeamSide.TEAM_1 else TeamSide.TEAM_2
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide == winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, f); f -= HEIGHT_PX }
        matchResult.playerStats
            .filter { (bp, _) -> bp.team.teamSide != winningTeam }
            .forEach { (bp, stats) -> createPlayerRow(bp, f); f -= HEIGHT_PX }
        val width = recalculateTabs()

        val team1Color = if (matchResult.team1Won == true) matchResult.map.team1.color else matchResult.map.team2.color
        val team1Num = if (matchResult.team1Won == true) matchResult.map.team1.size() else matchResult.map.team2.size()
        val team2Color = if (matchResult.team1Won == false) matchResult.map.team1.color else matchResult.map.team2.color
        val team2Num = if (matchResult.team1Won == false) matchResult.map.team1.size() else matchResult.map.team2.size()
        team1BgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
            text(Component.text(" "))
            backgroundColor = Color.fromARGB(100, team1Color.red(), team1Color.green(), team1Color.blue())
            transformation = transformation.apply {
                translation.set(X_*width, -(2 + team1Num)*HEIGHT_PX, 0f)
                scale.set(width / textWidth(" "), team1Num.toFloat(), 1f)
            }
            billboard = Display.Billboard.VERTICAL
        }

        team2BgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
            text(Component.text(" "))
            backgroundColor = Color.fromARGB(100, team2Color.red(), team2Color.green(), team2Color.blue())
            transformation = transformation.apply {
                translation.set(X_*width, -(2 + team1Num + team2Num)*HEIGHT_PX, 0f)
                scale.set(width / textWidth(" "), team2Num.toFloat(), 1f)
            }
            billboard = Display.Billboard.VERTICAL
        }
        tabs.forEach { it.init() }
        listOf(team1BgDisplay, team2BgDisplay).forEach { it.interpolationDuration = ANIMATION_TICKS }
    }

    fun recalculateTabs(): Float {
        val totalWidth = tabs.sumOf { it.layoutWidth.toDouble() }.toFloat()
        var x = - totalWidth / 2
        tabs.forEach {
            x += it.layoutWidth/2
            it.layoutX = x
            x += it.layoutWidth/2
        }
        listOfNotNull(team1BgDisplay, team2BgDisplay).forEach {
            it.animate {
                translation.x = X_*totalWidth
                scale.x = totalWidth / textWidth(" ")
            }
        }
        return totalWidth
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

    fun formatPercentage(total: Int, given: Int): TextComponent {
        val percentage = (given * 100) / total
        val color: Int = when {
            percentage == 100 -> 0x40FF40
            percentage > 90 -> 0x80FF80
            percentage > 80 -> 0xC0FFC0
            percentage > 64 -> 0xE0FFD0
            percentage > 50 -> 0xFFE0D0
            percentage > 32 -> 0xFFC0C0
            percentage > 20 -> 0xFF8080
            else -> 0xFF4040
        }
        return Component.text("$percentage%", TextColor.color(color))
    }

    fun createPlayerRow(bp: BotBowsPlayer, offset: Double) {
        val playerNameDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), TextDisplay::class.java).apply {
            text(bp.name)
            val offset = textWidth(bp.plainName) / 2
            transformation = transformation.apply { translation.set(X - 2.5f - 6 * PX - offset, 0f, 0.01f); }
            billboard = Display.Billboard.VERTICAL
        }

        val playerHeadDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), ItemDisplay::class.java).apply {
            setItemStack(bp.avatar.headItem)
            transformation = transformation.apply {
                translation.set(-2.4f, 8 * PX, 0.01f)
                scale.set(0.25f, 0.25f, 0.25f)
                leftRotation.set(AxisAngle4f(Math.toRadians(180.0).toFloat(), 0f, 1f, 0f))
            }
            billboard = Display.Billboard.VERTICAL
        }

        displays.addAll(listOf(playerNameDisplay, playerHeadDisplay))
    }

    fun remove() {
        displays.forEach { it.remove() }
        hitsTab.remove()
        deathsTab.remove()
    }
}