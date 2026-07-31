package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.joml.AxisAngle4f

const val PX = 0.025f
const val X = -PX/2f // Workaround to undo mojangs hardcoded bug that offsets text for no reason (textshadow that isnt there)

class ResultDisplay(val loc: Location, matchResult: MatchResult) {

    val titleBgDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text(" "))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(X*20, 0f, 0f) }

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
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(X*20, 0f, 0f) }
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
        var f = -0.375
        matchResult.playerStats.forEach { (uuid, stats) ->
            f -= 0.25
            val bp: BotBowsPlayer = BotBows.getBotBowsPlayer(uuid)
            createPlayerRow(bp, stats, f)
        }
    }

    fun createPlayerRow(bp: BotBowsPlayer, stats: PlayerMatchStats, offset: Double) {
        val playerNameDisplay = Main.WORLD.spawn(loc.clone().add(0.0, offset, 0.0), TextDisplay::class.java).apply {
            text(bp.name)
            alignment = TextDisplay.TextAlignment.RIGHT
            transformation = transformation.apply { translation.set(X -2 - 10*PX, 0f, 0.01f); }
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
    }
}