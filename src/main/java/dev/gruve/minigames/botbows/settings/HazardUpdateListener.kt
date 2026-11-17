package dev.gruve.minigames.botbows.settings

import dev.gruve.minigames.botbows.hazard.HazardType

interface HazardUpdateListener {
    fun onHazardUpdate(type: HazardType)
    fun onSchemaUpdate() // when all hazards are changed, for example when switching map
}