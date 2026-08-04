package gruvexp.bbminigames.model.stat

const val PX = 0.025f
const val HEIGHT_PX = 10*PX.toDouble()
const val X = -PX/2f // Workaround to undo mojangs hardcoded bug that offsets text for no reason (textshadow that isnt there)

fun textWidth(text: String, scale: Float = 1f): Float {
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