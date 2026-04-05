package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.model.preset.AbilityPreset
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.botbowsTeams.TeamSide

class AbilitySettings {
    var maxAbilities = 0
        set(value) {
            val toggle: Boolean = field == 0 || value == 0
            field = value
            if (toggle) notifyAbilitiesToggle() else notifyMaxAbilities()
        }

    var isIndividualMax = false
        set(value) {
            field = value
            notifyIndividualMaxToggle()
        }

    var cooldownMultiplier = 1.0f
        set(value) {
            field = value
            notifyCooldown()
        }

    var isIndividualCooldown = false
        set(value) {
            field = value
            notifyIndividualCooldownToggle()
        }

    var isUniqueMode = false
        set(value) {
            field = value
            notifyUniqueModeToggle()
        }

    private val teamAbilities: Map<TeamSide, MutableMap<AbilityType, BotBowsPlayer>> = mapOf(
        TeamSide.TEAM_1 to mutableMapOf(),
        TeamSide.TEAM_2 to mutableMapOf())

    private val bannedAbilities = mutableSetOf<AbilityType>()
    private val listeners = mutableMapOf<BotBowsPlayer, AbilityUpdateListener>()

    fun addListener(bp: BotBowsPlayer, listener: AbilityUpdateListener) {
        listeners[bp] = listener
    }

    fun removeListener(bp: BotBowsPlayer) {
        listeners.remove(bp)
    }

    fun ban(type: AbilityType) {
        bannedAbilities.add(type)
        notifyStatus(type)
    }

    fun unban(type: AbilityType) {
        bannedAbilities.remove(type)
        notifyStatus(type)
    }

    fun toggle(type: AbilityType) {
        if (isBanned(type)) unban(type) else ban(type)
    }

    fun isBanned(type: AbilityType): Boolean {
        return bannedAbilities.contains(type)
    }

    fun getBanned(): Set<AbilityType> {
        return bannedAbilities
    }

    fun applyPreset(preset: AbilityPreset) {
        preset.maxAbilities?.let { maxAbilities = it }

        preset.individualMaxAbilities?.forEach { (uuid, max) ->
            BotBows.getBotBowsPlayer(uuid)?.maxAbilities = max
        }
        preset.cooldownMultiplier?.let { cooldownMultiplier = it }

        preset.individualCooldownMultiplier?.forEach { (uuid, cooldown) ->
            BotBows.getBotBowsPlayer(uuid)?.abilityCooldownMultiplier = cooldown
        }
        preset.bannedAbilities?.let { it ->
            val changed = bannedAbilities xor it
            bannedAbilities.retainAll(it)
            bannedAbilities.addAll(it)
            changed.forEach { notifyStatus(it) }
        }
    }

    private fun notifyAbilitiesToggle() {
        listeners.values.forEach { it.onAbilitiesToggle() }
    }

    private fun notifyMaxAbilities() {
        listeners.values.forEach { it.onMaxAbilitiesChange() }
    }

    private fun notifyIndividualMaxToggle() {
        listeners.values.forEach { it.onIndividualMaxToggle() }
    }

    private fun notifyCooldown() {
        listeners.values.forEach { it.onCooldownMultiplierChange() }
    }

    private fun notifyIndividualCooldownToggle() {
        listeners.values.forEach { it.onIndividualCooldownToggle() }
    }

    private fun notifyUniqueModeToggle() {
        listeners.values.forEach { it.onUniqueModeToggle() }
    }

    private fun notifyStatus(type: AbilityType) {
        listeners.values.forEach { it.onAbilityStatusChange(type) }
    }

    fun attemptEquip(bp: BotBowsPlayer, type: AbilityType): Boolean {
        if (!isUniqueMode) return true
        val equipped = teamAbilities[bp.team.teamSide]!!
        val currentBp = equipped[type]

        if (currentBp == null || currentBp == bp) {
            if (currentBp == null) {
                equipped[type] = bp
                listeners.values.forEach { it.onUniqueAbilityOccupancyChange(type, bp, true) }
            }
            return true
        }
        return false
    }

    fun unequip(bp: BotBowsPlayer, type: AbilityType) {
        teamAbilities[bp.team.teamSide]!!.remove(type)
        listeners.values.forEach { it.onUniqueAbilityOccupancyChange(type, bp, false) }
    }


    private infix fun <T> Set<T>.xor(other: Set<T>): Set<T> {
        val intersect = this.intersect(other)
        return (this + other) - intersect
    }
}