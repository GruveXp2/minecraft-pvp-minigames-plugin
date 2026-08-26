package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityContext.Launch
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnLaunch
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnProjectileHit
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector

class SplashBow(bp: BotBowsPlayer, hotBarSlot: Int) : Ability(bp, hotBarSlot, AbilityType.SPLASH_BOW), OnLaunch, OnProjectileHit {
    init {
        bp.avatar.setItem(18, ItemStack(Material.ARROW, 64))
    }

    override fun onLaunch(ctx: Launch) {
        if (ctx.projectile is Arrow) {
            use()
            val arrow = ctx.projectile
            arrow.color = Color.RED
            val arrowTrail = SplashArrowTrailGenerator(arrow, bp.team.dyeColor.color)
                .runTaskTimer(Main.getPlugin(), 1L, 1L)
            arrow.velocity.multiply(0.5f)
            activeArrows[arrow] = arrowTrail
            arrow.setMetadata("botbows_ability", FixedMetadataValue(Main.getPlugin(), this))
        } else {
            throw IllegalArgumentException("Splash bow tried to fire something that wasnt an arrow")
        }
    }

    override fun onHit(e: ProjectileHitEvent) {
        val arrow = e.getEntity() as Arrow
        val shooter = arrow.shooter as Player
        val hitLoc = e.hitEntity?.location ?: e.hitBlock!!.location
        val shooterBp = BotBows.getBotBowsPlayer(shooter) ?: return

        handleArrowHit(shooterBp, hitLoc)
        activeArrows.remove(arrow)?.cancel()
        arrow.remove()
    }

    class SplashArrowTrailGenerator(private val arrow: Arrow, private val color: Color) : BukkitRunnable() {
        override fun run() {
            arrow.world.spawnParticle(
                Particle.DUST,
                arrow.location,
                5,
                0.1,
                0.1,
                0.1,
                0.1,
                DustOptions(Color.RED, 3f),
                true
            )
            arrow.world.spawnParticle(
                Particle.DUST,
                arrow.location,
                20,
                0.5,
                0.5,
                0.5,
                0.4,
                DustOptions(color, 2f),
                true
            )
            arrow.velocity.add(Vector(0.0, 0.03, 0.0))
        }
    }

    companion object {
        const val BLAST_RADIUS: Double = 3.0

        var activeArrows = mutableMapOf<Arrow, BukkitTask>()

        fun handleArrowHit(attacker: BotBowsPlayer, hitLoc: Location) {
            val attackerTeamColor = attacker.team.dyeColor.color
            val world = attacker.avatar.location.world
            world.spawnParticle(
                Particle.EXPLOSION_EMITTER,
                hitLoc,
                5,
                BLAST_RADIUS / 4,
                BLAST_RADIUS / 4,
                BLAST_RADIUS / 4,
                5.0
            )
            world.spawnParticle(
                Particle.DUST,
                hitLoc,
                1000,
                2.0,
                2.0,
                2.0,
                0.4,
                DustOptions(attackerTeamColor, 5f)
            ) // Red color
            val ability = attacker.getAbility(AbilityType.SPLASH_BOW)
            hitLoc.world.getNearbyEntities(hitLoc, BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)
                .mapNotNull { BotBows.getBotBowsPlayer(it.uniqueId) }
                .filter { it.isAlive }
                .forEach { defender ->
                    defender.damage(DamageContext.Player(DamageType.Player.SPLASH_BOW, attacker))
                    if (defender != attacker) ability.registerSuccess() else ability.registerFail()
                }
        }
    }
}
