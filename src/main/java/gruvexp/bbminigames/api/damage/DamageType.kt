package gruvexp.bbminigames.api.damage

sealed interface DamageType {
    val template: String

    // damaged by the environment
    enum class Environment(override val template: String) : DamageType {
        LAVA("%d tried to swim in lava"),
        LIGHTNING("%d was electrocuted to a crisp");
    }

    // Enum for alle spillervåpen
    enum class Player(override val template: String) : DamageType {
        LASER("%d was enlightened by %a's laser"),
        BOW("%d was sniped by %a");
    }
}