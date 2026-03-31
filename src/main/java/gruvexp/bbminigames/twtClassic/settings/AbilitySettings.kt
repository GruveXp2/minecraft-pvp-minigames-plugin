package gruvexp.bbminigames.twtClassic.settings

import gruvexp.bbminigames.model.preset.AbilityPreset
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType

class AbilitySettings {
    var maxAbilities = 0
        set(value) {
            field = value
            if (field == 0 || value == 0) notifyAbilitiesToggle() else notifyMaxAbilities()
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

    private fun notifyStatus(type: AbilityType) {
        listeners.values.forEach { it.onAbilityStatusChange(type) }
    }

    private infix fun <T> Set<T>.xor(other: Set<T>): Set<T> {
        val intersect = this.intersect(other)
        return (this + other) - intersect
    }
}