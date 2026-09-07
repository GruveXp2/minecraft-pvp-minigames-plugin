package gruvexp.bbminigames.util

import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

// inline = not its own function but just copypasted into code at compile time
// reified: if its off the compiler only uses the T to check and give errors if u use it wrong. if its on, the type actually stays so you can use it further
inline fun <reified T : BlockData> Block.editData(action: T.() -> Unit) {
    blockData = (blockData as T).apply(action)
}