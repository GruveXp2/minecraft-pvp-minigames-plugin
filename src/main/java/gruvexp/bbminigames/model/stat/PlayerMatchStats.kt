package gruvexp.bbminigames.model.stat

import java.util.UUID

data class PlayerMatchStats(
    val playerId: UUID,
    var kills: Int = 0,
    var deaths: Int = 0,
    var hits: Int = 0,
    var damage: Int = 0,
)  {
    fun addKill() { kills++ }
    fun addDeath() { deaths++ }
    fun addHit() { hits++ }
    fun addDamage() { damage++ }
}