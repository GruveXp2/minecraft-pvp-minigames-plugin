package gruvexp.bbminigames.util

import gruvexp.bbminigames.Main
import net.kyori.adventure.text.format.TextColor
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.scheduler.BukkitRunnable

// inline = not its own function but just copypasted into code at compile time
// reified: if its off the compiler only uses the T to check and give errors if u use it wrong. if its on, the type actually stays so you can use it further
inline fun <reified T : BlockData> Block.editData(action: T.() -> Unit) {
    blockData = (blockData as T).apply(action)
}

fun TextColor.lighten(factor: Float): TextColor {

    val r = ((255 - red()) * factor)
    val g = ((255 - green()) * factor)
    val b = ((255 - blue()) * factor)

    return TextColor.color(r, g, b)
}

fun World.setTimeSmooth(start: Long, end: Long, seconds: Int) {
    val ticks = seconds * 20
    val step = (end - start) / ticks
    object : BukkitRunnable() {
        var count: Long = 0
        override fun run() {
            if (count >= ticks) cancel()
            else time = start + (count++ * step)
        }
    }.runTaskTimer(Main.getPlugin(), 0, 1)
}