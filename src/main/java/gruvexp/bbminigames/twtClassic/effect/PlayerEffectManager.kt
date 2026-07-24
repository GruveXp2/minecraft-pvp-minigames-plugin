package gruvexp.bbminigames.twtClassic.effect

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class PlayerEffectManager(private val bp: BotBowsPlayer) {

    companion object {
        private const val DEFAULT_ANIMATION_TICKS = 10 // how long to transition from one size to another
        private const val GLOW_TICK_PERIOD = 1L
    }

    enum class ScalePriority { NORMAL, OVERRIDE }

    enum class ScaleSource { BABY_POTION, STEAM_PIPE, GROW_KARMA, GROW_TRAP }

    enum class GlowSource(val priority: Int) {
        DEBUFF(1),
        RADAR(2),
        HIT_COOLDOWN(3)
    }
    // represents an effect that is currently on the player
    private data class ScaleContribution(val targetScale: Double, val priority: ScalePriority)
    private data class GlowContribution(val color: NamedTextColor?, val blinkPeriodTicks: Int)

    private val avatar: BotBowsAvatar
        get() = bp.avatar

    private val scaleContributions = HashMap<ScaleSource, ScaleContribution>()
    private val scaleExpiry = HashMap<ScaleSource, BukkitTask>() // tickers for the current effects, that will remove the effects when reaching zero.
    private var scaleTween: BukkitTask? = null // task for smoothly transitioning/interpolating to the new size

    private val glowContributions = HashMap<GlowSource, GlowContribution>()
    private val glowExpiry = HashMap<GlowSource, BukkitTask>()
    private var glowTicker: BukkitTask? = null
    private var glowElapsedTicks = 0 // used for blink phase (its just to have a number that ++es each tick so we can % it)

    @JvmOverloads // java compat bc 3 last args are optional (it generates overloads based on usages)
    fun applyScale(
        source: ScaleSource,
        targetScale: Double,
        priority: ScalePriority = ScalePriority.NORMAL,
        durationTicks: Long? = null,
        animationTicks: Int = DEFAULT_ANIMATION_TICKS
    ) {
        scaleContributions[source] = ScaleContribution(targetScale, priority)
        scaleExpiry.remove(source)?.cancel()
        if (durationTicks != null) {
            scaleExpiry[source] = Bukkit.getScheduler()
                .runTaskLater(Main.getPlugin(), Runnable { clearScale(source) }, durationTicks)
        }
        recalculateScale(animationTicks)
    }

    fun clearScale(source: ScaleSource) {
        if (!scaleContributions.containsKey(source)) return
        scaleContributions.remove(source)
        scaleExpiry.remove(source)?.cancel()
        recalculateScale(DEFAULT_ANIMATION_TICKS)
    }

    private fun getTargetScale(): Double {
        val override = scaleContributions.values.filter { it.priority == ScalePriority.OVERRIDE }
        return when {
            override.isNotEmpty() -> override.last().targetScale // overrides like steam pipes will force a scale and wont avg
            scaleContributions.isEmpty() -> 1.0
            else -> scaleContributions.values.map { it.targetScale }.average() // avg of current effects (for example, if u have both shrink and grow, the result will be in between)
        }
    }

    private fun recalculateScale(animationTicks: Int) {
        scaleTween?.cancel()
        scaleTween = null
        val target = getTargetScale()
        val start = avatar.getScale()
        if (start == target) return
        scaleTween = object : BukkitRunnable() {
            var i = 1
            override fun run() {
                avatar.setScale(start + (target - start) / animationTicks * i)
                if (i >= animationTicks) cancel()
                i++
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L)
    }

    @JvmOverloads
    fun applyGlow(source: GlowSource, durationTicks: Long? = null, color: NamedTextColor? = null, blinkPeriodTicks: Int = 10) {
        glowContributions[source] = GlowContribution(color, blinkPeriodTicks)
        glowExpiry.remove(source)?.cancel()
        if (durationTicks != null) {
            glowExpiry[source] = Bukkit.getScheduler()
                .runTaskLater(Main.getPlugin(), Runnable { clearGlow(source) }, durationTicks)
        }
        if (glowTicker == null) startGlowTicker()
    }

    fun clearGlow(source: GlowSource) {
        if (!glowContributions.containsKey(source)) return
        glowContributions.remove(source)
        glowExpiry.remove(source)?.cancel()
        if (glowContributions.isEmpty()) {
            glowTicker?.cancel()
            glowTicker = null
            avatar.setGlowing(false)
            avatar.setColor(teamColor())
        }
    }

    private fun startGlowTicker() {
        glowElapsedTicks = 0
        glowTicker = object : BukkitRunnable() { // always runs aslong as there is a glow effect
            override fun run() {
                avatar.setGlowing(true)
                val winner = glowContributions.entries.maxByOrNull { it.key.priority }?.value
                val showColor = winner?.color != null &&
                    (glowElapsedTicks % winner.blinkPeriodTicks) < winner.blinkPeriodTicks / 2
                avatar.setColor(if (showColor) winner.color else teamColor())
                glowElapsedTicks += GLOW_TICK_PERIOD.toInt()
            }
        }.runTaskTimer(Main.getPlugin(), 0L, GLOW_TICK_PERIOD)
    }

    private fun teamColor(): NamedTextColor = avatar.botBowsPlayer.teamColor as NamedTextColor

    fun clear() {
        scaleTween?.cancel()
        scaleTween = null
        scaleContributions.clear()
        scaleExpiry.values.forEach { it.cancel() }
        scaleExpiry.clear()
        avatar.setScale(1.0)

        glowTicker?.cancel()
        glowTicker = null
        glowContributions.clear()
        glowExpiry.values.forEach { it.cancel() }
        glowExpiry.clear()
        avatar.setGlowing(false)
        avatar.setColor(teamColor())

        avatar.entity.clearActivePotionEffects()
    }
}