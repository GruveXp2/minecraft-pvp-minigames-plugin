package gruvexp.bbminigames.model.stat

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay

const val MJNG_UNBUG = -0.0125f // Workaround to undo mojangs hardcoded bug that offsets text for no reason

class ResultDisplay(loc: Location, matchResult: MatchResult) {

    val titleBgDisplay: TextDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text("X"))
        backgroundColor = Color.fromARGB(100, 32, 50, 100)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }

        billboard = Display.Billboard.VERTICAL
    }

    val titleDisplay: TextDisplay = Main.WORLD.spawn(loc, TextDisplay::class.java).apply {
        text(Component.text("MATCH RESULTS"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(MJNG_UNBUG, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val headerBgDisplay: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("X"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }

    val killsHeaderDisplay: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("kills"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(MJNG_UNBUG-1f, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val deathsHeaderDisplay: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("deaths"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(MJNG_UNBUG, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }

    val kdRatioHeaderDisplay: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, -0.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("k/d"))
        backgroundColor = Color.fromARGB(0)
        transformation = transformation.apply { translation.set(MJNG_UNBUG+1, 0f, 0.01f); }
        billboard = Display.Billboard.VERTICAL
    }





    val test1: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 0.5, 0.0), TextDisplay::class.java).apply {
        text(Component.text("."))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }
    val test2: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 0.75, 0.0), TextDisplay::class.java).apply {
        text(Component.text("l"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }
    val test3: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 1.0, 0.0), TextDisplay::class.java).apply {
        text(Component.text("t"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }
    val test4: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 1.25, 0.0), TextDisplay::class.java).apply {
        text(Component.text("k"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }
    val test5: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 1.5, 0.0), TextDisplay::class.java).apply {
        text(Component.text("ø"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(20f, 1f, 1f); translation.set(MJNG_UNBUG*20, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }
    val test6: TextDisplay = Main.WORLD.spawn(loc.clone().add(0.0, 1.75, 0.0), TextDisplay::class.java).apply {
        text(Component.text("ø"))
        alignment = TextDisplay.TextAlignment.CENTER
        backgroundColor = Color.fromARGB(50, 100, 32, 50)
        transformation = transformation.apply { scale.set(1f, 1f, 1f); translation.set(MJNG_UNBUG, 0f, 0f) }
        billboard = Display.Billboard.VERTICAL
    }

}