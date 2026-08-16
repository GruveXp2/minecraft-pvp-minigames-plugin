package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector


class ResultDisplay(val loc: Location, matchResult: MatchResult) {

    val displays: MutableSet<Display> = mutableSetOf()
    val tabs: MutableSet<StatTab> = mutableSetOf()

    val playerTab = PlayerTab(
        loc,
        -HEIGHT_PX,
        matchResult.playerStats.values.filter { it.bp.team.teamSide == matchResult.winningTeam }.map { it.bp },
        matchResult.playerStats.values.filter { it.bp.team.teamSide != matchResult.winningTeam }.map { it.bp }
    )

    val hitsTab = ColTab("Hits", loc, -HEIGHT_PX) { recalculateTabs() }.also { tab ->
        tab.addColumns(listOf(
            StatCol(
                "hits",
                loc,
                tab,
                -HEIGHT_PX,
                { Component.text(it.hits, NamedTextColor.GREEN) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "dmg",
                loc,
                tab,
                -HEIGHT_PX,
                { Component.text(it.damage, NamedTextColor.RED) },
                matchResult.playerStats.values.toList()
            ),
            StatCol(
                "h/d",
                loc,
                tab,
                -HEIGHT_PX,
                { formatRatio(it.hits, it.damage) },
                matchResult.playerStats.values.toList()
            )
        ))
        tabs.add(tab)
    }

    val deathsTab = ColTab("Deaths", loc, -HEIGHT_PX) { recalculateTabs() }.also { tab ->
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

    val abilityTab = AbilityTab("Abilities", loc, -HEIGHT_PX, matchResult.playerStats.values.toList() ) { recalculateTabs() }.also { tab ->
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

    private var playerScanner: BukkitTask? = null

    init {
        displays.addAll(listOf(titleBgDisplay, titleDisplay))

        val width = recalculateTabs()
        val team1Color = if (matchResult.team1Won == true) matchResult.map.team1.color else matchResult.map.team2.color
        val team1Num = if (matchResult.team1Won == true) matchResult.map.team1.size() else matchResult.map.team2.size()
        team1BgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
            text(Component.text(" "))
            backgroundColor = Color.fromARGB(100, team1Color.red(), team1Color.green(), team1Color.blue())
            transformation = transformation.apply {
                translation.set(X_*width, -(2 + team1Num)*HEIGHT_PX, 0f)
                scale.set(width / textWidth(" "), team1Num.toFloat(), 1f)
            }
            billboard = Display.Billboard.VERTICAL
        }

        val team2Color = if (matchResult.team1Won == false) matchResult.map.team1.color else matchResult.map.team2.color
        val team2Num = if (matchResult.team1Won == false) matchResult.map.team1.size() else matchResult.map.team2.size()
        team2BgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
            text(Component.text(" "))
            backgroundColor = Color.fromARGB(100, team2Color.red(), team2Color.green(), team2Color.blue())
            transformation = transformation.apply {
                translation.set(X_*width, -(2 + team1Num + team2Num)*HEIGHT_PX, 0f)
                scale.set(width / textWidth(" "), team2Num.toFloat(), 1f)
            }
            billboard = Display.Billboard.VERTICAL
        }
        playerTab.init()
        tabs.forEach { it.init() }
        listOf(team1BgDisplay, team2BgDisplay).forEach { it.interpolationDuration = ANIMATION_TICKS }

        playerScanner = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), Runnable {
            loc.getNearbyPlayers(10.0).forEach { p ->
                rayTrace(p.eyeLocation.toVector(), p.eyeLocation.direction, clickingPlayers.contains(p))
                clickingPlayers.remove(p)
            }
        }, 0, 1)
    }

    fun recalculateTabs(): Float {
        val totalWidth = tabs.sumOf { it.layoutWidth.toDouble() }.toFloat()
        var x = - totalWidth / 2
        playerTab.layoutX = x
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

    fun rayTrace(eyeLoc: Vector, eyeDir: Vector, didClick: Boolean) {
        // location of display (aka ResultDisplay) will be the origo
        eyeLoc.subtract(loc.toVector()) // eyeLoc relative to new origo

        // Matrix describing coordinate system aligned with display (variables prefixed with d- means theyre in this coord system)
        // bc the display has vertical billboard, y-axis is the same as global y-axis: dYVec = Vector(0, 1, 0)
        val dXVec = Vector(-eyeDir.z, 0.0, eyeDir.x).normalize() // need to be 1 long since thats how long the transformation.xy vectors are
        val dZVec = Vector(eyeDir.x, 0.0, eyeDir.z).normalize()

        // transforming to display coords
        val dEyeLoc = Vector(eyeLoc.dot(dXVec), eyeLoc.y, eyeLoc.dot(dZVec))
        val dEyeDir = Vector(eyeDir.dot(dXVec), eyeDir.y, eyeDir.dot(dZVec))

        // finding how long to step in dEyeDir until hitting z=0 (where the display is)
        val t = - dEyeLoc.z / dEyeDir.z // if dEyeDir was normalized, this would be amount of blocks between eyeloc and where the ray hits

        if (t < 0) return // if we have to step backwards to hit the display plane, it means the player is looking the opposite way aka not looking at the display

        // stepping that exact distance. now x and z will be the translation on the display, since z = 0
        val dX = dEyeLoc.x + t * dEyeDir.x
        val dY = dEyeLoc.y + t * eyeDir.y

        handleLook(didClick, dX.toFloat(), dY.toFloat())
    }

    fun handleLook(didClick: Boolean, x: Float, y: Float) {
        if (y < -HEIGHT_PX - 2*PX || y > 2*PX) return

        for (tab: StatTab in tabs) {
            if (x > tab.layoutX - tab.layoutWidth / 2 && x < tab.layoutX + tab.layoutWidth / 2) {
                if (didClick) tab.apply { isExpanded = !isExpanded }
                tab.apply { isHovered = true}
                return
            }
        }
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

    fun remove() {
        displays.forEach { it.remove() }
        playerTab.remove()
        hitsTab.remove()
        deathsTab.remove()
        abilityTab.remove()
        playerScanner?.cancel()
    }

    companion object {
        val clickingPlayers: MutableSet<Player> = mutableSetOf()

        fun registerPlayerClick(p: Player) { // to find out if a player just clicked
            clickingPlayers.add(p)
        }
    }
}