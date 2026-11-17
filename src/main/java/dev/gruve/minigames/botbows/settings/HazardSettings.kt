package dev.gruve.minigames.botbows.settings

import dev.gruve.minigames.botbows.map.BotBowsMap
import dev.gruve.minigames.botbows.hazard.Hazard
import dev.gruve.minigames.botbows.hazard.HazardChance
import dev.gruve.minigames.botbows.hazard.HazardType
import java.util.EnumMap

class HazardSettings {
    private val hazards = EnumMap<HazardType, Hazard>(HazardType::class.java).apply {
        HazardType.entries.forEach { put(it, it.createHazard()) }
    }

    var listener: HazardUpdateListener? = null
        set(value) {
            field = value
            listener?.onSchemaUpdate()
        }

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
            type.createHazard().apply { chance = config.chance }
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
