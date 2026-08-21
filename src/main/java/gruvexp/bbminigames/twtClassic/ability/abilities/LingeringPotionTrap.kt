package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnLingeringPotionUse
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

open class LingeringPotionTrap(bp: BotBowsPlayer, hotBarSlot: Int)
    : Ability(bp, hotBarSlot, AbilityType.LINGERING_POTION), OnLingeringPotionUse {
    fun addSizeIncreaseAreaEffect(loc: Location) {
        val throwerBp = bp
        object : BukkitRunnable() {
            var counter: Int = 0

            override fun run() {
                if (counter++ >= DURATION * 10) {
                    cancel()
                    return
                }

                loc.world.getNearbyEntities(
                    loc,
                    LINGERING_POTION_RADIUS.toDouble(),
                    1.0,
                    LINGERING_POTION_RADIUS.toDouble()
                ).mapNotNull { BotBows.getBotBowsPlayer(it.uniqueId) }
                    .filter { it.team != throwerBp.team }
                    .forEach {
                        it.effectManager.applyScale(
                            PlayerEffectManager.ScaleSource.GROW_TRAP,
                            1.5,
                            PlayerEffectManager.ScalePriority.NORMAL,
                            (EFFECT_DURATION * 20).toLong()
                        )
                    }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 2L)
    }

    override fun reset() {
        cloudOwners.keys.forEach { it.remove() }
    }

    override fun destroy() {
        cloudOwners.keys.forEach { it.remove() }
    }

    override fun onSplash(e: LingeringPotionSplashEvent) {
        val cloud = e.areaEffectCloud
        cloud.reapplicationDelay = EFFECT_DURATION * 10
        cloudOwners[cloud] = bp

        val potion = e.entity
        val hasUnluck = potion.effects.any { it.type == PotionEffectType.UNLUCK }
        if (hasUnluck) {
            addSizeIncreaseAreaEffect(potion.location)
        }
    }

    override fun onCloudApply(e: AreaEffectCloudApplyEvent) {
        val cloudEffect: PotionEffect = e.entity.customEffects[0]
        val effectType = cloudEffect.type
        val glowDuration = (cloudEffect.duration / 4).toLong() // only get 25% duration from the area effect cloud

        val it = e.affectedEntities.iterator()
        while (it.hasNext()) {
            val entity = it.next()
            val affectedBp = BotBows.getBotBowsPlayer(entity.uniqueId) ?: continue
            if (affectedBp.team == bp.team) { // dont affect team of thrower
                it.remove()
                continue
            }
            onEffectReceive(affectedBp, effectType, glowDuration)
        }
    }

    private fun onEffectReceive(affectedBp: BotBowsPlayer, effectType: PotionEffectType, glowDuration: Long) {
        affectedBp.effectManager.applyGlow(
            PlayerEffectManager.GlowSource.DEBUFF,
            glowDuration,
            NamedTextColor.GOLD,
            10
        )
        val effectName = if (effectType == PotionEffectType.UNLUCK) "GROWING" else effectType.key.value()
        affectedBp.lobby.messagePlayers(
            Component.text("", BotBows.lighten(bp.team.color, 0.5))
                .append(affectedBp.name)
                .append(Component.text(" took a bath in "))
                .append(bp.name)
                .append(Component.text("'s lingering potion cloud and got "))
                .append(Component.text(effectName, NamedTextColor.DARK_RED))
        )
        registerSuccess()
    }

    companion object {
        const val DURATION: Int = 30 // how long the potion lingers on the ground
        const val EFFECT_DURATION: Int = 20 // how long you have the effect after stepping into it
        const val LINGERING_POTION_RADIUS: Int = 3

        private val EFFECTS = arrayOf(
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.DARKNESS,
            PotionEffectType.UNLUCK
        )

        private val EFFECT_COLORS = mapOf(
            PotionEffectType.SLOWNESS to Color.fromRGB(90, 90, 255),
            PotionEffectType.LEVITATION to Color.fromRGB(255, 255, 255),
            PotionEffectType.BLINDNESS to Color.fromRGB(0, 0, 0),
            PotionEffectType.UNLUCK to Color.fromRGB(128, 100, 32)
        )

        protected var cloudOwners: MutableMap<AreaEffectCloud, BotBowsPlayer> =
            mutableMapOf()

        @JvmStatic
        fun getCloudOwner(cloud: AreaEffectCloud): BotBowsPlayer? {
            return cloudOwners[cloud]
        }

        @JvmStatic
        fun giveRandomEffect(thrownPotion: ThrownPotion) {
            val randomEffect: PotionEffectType = EFFECTS[BotBows.RANDOM.nextInt(EFFECTS.size)]
            val potionColor = EFFECT_COLORS[randomEffect] ?: Color.GRAY

            val potion = thrownPotion.item
            potion.editMeta(PotionMeta::class.java) {
                it.clearCustomEffects()
                it.addCustomEffect(
                    PotionEffect(randomEffect, EFFECT_DURATION * 20 * 4, 2),
                    true
                )
                it.color = potionColor
            }
            thrownPotion.item = potion
        }
    }
}
