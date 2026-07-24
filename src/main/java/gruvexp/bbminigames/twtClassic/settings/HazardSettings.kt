package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.hazard.Hazard
import gruvexp.bbminigames.twtClassic.hazard.HazardChance
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import java.util.EnumMap

class HazardSettings {
    private val hazards = EnumMap<HazardType, Hazard>(HazardType::class.java)

    var listener: HazardUpdateListener? = null

    fun setChance(type: HazardType, chance: HazardChance) {
        hazards[type]?.let {
            it.chance = chance
            listener?.onHazardUpdate(type)
        }
    }

    fun getChance(type: HazardType): HazardChance? {
        return hazards[type]?.chance
    }

    fun getChances(): Map<HazardType, HazardChance> {
        return hazards.mapValues { it.value.chance }
    }

    fun getHazardTypes(): Set<HazardType> {
        return hazards.keys
    }

    fun getHazard(type: HazardType): Hazard? {
        return hazards[type]
    }

    fun createActiveHazards(): Set<Hazard> {
        return hazards.map { (type, config) ->
            type.createHazard().apply { this.chance = config.chance }
        }.toSet()
    }

    fun syncWithMap(map: BotBowsMap) {
        hazards.keys.retainAll(map.allowedHazards) // remove hazards not compatible with the new map
        map.allowedHazards.forEach { type ->
            hazards.computeIfAbsent(type) { type.createHazard() }
        }
        listener?.onSchemaUpdate()
    }
}
