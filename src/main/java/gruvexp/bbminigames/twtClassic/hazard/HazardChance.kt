package gruvexp.bbminigames.twtClassic.hazard

enum class HazardChance(val percent: Int, private val chanceString: String) {
    ALWAYS(100, "ALWAYS"),
    FIFTY(50, "50%"),
    TWENTY_FIVE(25, "25%"),
    TEN(10, "10%"),
    FIVE(5, "5%"),
    DISABLED(0, "DISABLED");

    override fun toString(): String {
        return chanceString
    }

    fun rollChance(): Boolean {
        return (0..100).random() <= percent
    }

    companion object {
        val PERCENT_STRINGS: List<String> = listOf("5%", "10%", "25%", "50%", "ALWAYS")

        fun of(chanceString: String): HazardChance {
            for (chance in entries) {
                if (chance.chanceString.equals(chanceString, ignoreCase = true)) {
                    return chance
                }
            }
            throw IllegalArgumentException("Invalid HazardChance string: $chanceString")
        }
    }
}
