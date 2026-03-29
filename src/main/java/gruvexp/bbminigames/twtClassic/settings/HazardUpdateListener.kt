package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.hazard.HazardType

interface HazardUpdateListener {
    fun onHazardUpdate(type: HazardType)
    fun onSchemaUpdate() // when all hazards are changed, for example when switching map
}