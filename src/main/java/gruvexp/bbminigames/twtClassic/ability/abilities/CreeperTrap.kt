package gruvexp.bbminigames.twtClassic.ability.abilities

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityContext.EntityPlace
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnEntityPlace
import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.Ability
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.Lightable
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Creeper
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

open class CreeperTrap(bp: BotBowsPlayer, hotBarSlot: Int)
    : Ability(bp, hotBarSlot, AbilityType.CREEPER_TRAP), OnEntityPlace {

    var creeper: Creeper? = null
    var creeperTicker: CreeperTicker? = null

    override fun reset() {
        if (creeperTicker != null) creeperTicker!!.ignite()
    }

    override fun destroy() {
        if (creeperTicker != null) creeperTicker!!.destroy()
    }

    override fun trigger(ctx: EntityPlace) {
        use()

        val loc = ctx.loc
        // explode already placed creepers (so players cant farm creeper mines and trap another player completely)
        creeperOwners.entries
            .filter { it.value == bp }
            .map { it.key }
            .forEach { ignite(it) }

        creeper = bp.avatar.location.getWorld().spawn(loc, Creeper::class.java).apply {
            setAI(false)
            getAttribute(Attribute.SCALE)!!.baseValue = 0.75
        } .also { creeperOwners[it] = bp }

        loc.add(
            (-3 * CREEPER_PX).toDouble(),
            (23 * CREEPER_PX).toDouble(),
            (-3 * CREEPER_PX).toDouble()
        )

        // glass block that will be ontop of the creepers head
        val glassSize: Float = 6 * CREEPER_PX // headSize * % of headSize to use
        val glassDisplay = loc.getWorld().spawn(loc, BlockDisplay::class.java).apply {
            block = Bukkit.createBlockData(Material.getMaterial("${bp.team.dyeColor.name}_STAINED_GLASS")!!)
            transformation = transformation.apply { scale.set(glassSize, glassSize, glassSize) }
        }

        loc.add(CREEPER_PX.toDouble(), CREEPER_PX.toDouble(), CREEPER_PX.toDouble()) // creeper height

        // blinking redstone lamp inside the glass block
        val lampSize: Float = 4 * CREEPER_PX
        val lampDisplay = loc.getWorld().spawn(loc, BlockDisplay::class.java).apply {
            block = Bukkit.createBlockData(Material.WEATHERED_COPPER_BULB)
            transformation.apply { scale.set(lampSize, lampSize, lampSize) }
        }

        creeperTicker = CreeperTicker(creeper!!, lampDisplay, glassDisplay, bp).apply {
            runTaskTimer(Main.getPlugin(), ACTIVATION_DELAY * 20L, 5)
        }
    }

    class CreeperTicker(
        private val creeper: Creeper,
        private val lampDisplay: BlockDisplay,
        private val glassDisplay: BlockDisplay,
        private val owner: BotBowsPlayer
    ) : BukkitRunnable() {
        private var ticks = 0
        private var igniting = false
        private val hitPlayers: MutableSet<BotBowsPlayer> = mutableSetOf()

        override fun run() {
            ticks += 5
            if (ticks % 10 == 0 || igniting) {
                val lampData = lampDisplay.block as Lightable
                lampData.isLit = !lampData.isLit
                lampDisplay.block = lampData
            }
            if (ticks == 0) {
                lampDisplay.remove()
                glassDisplay.remove()
                cancel()
                explode()
            }
            for (p in creeper.world.getNearbyEntitiesByType(Player::class.java, creeper.location, BLAST_RADIUS)) {
                if (!p.hasLineOfSight(creeper)) continue
                val bp = BotBows.getBotBowsPlayer(p) ?: continue
                if (bp.lobby != owner.lobby) continue
                if (bp.team == owner.team && bp.avatar.location.distanceSquared(creeper.location) > 1) continue
                if (!bp.isAlive) continue

                hitPlayers.add(bp)
            }
            if (!hitPlayers.isEmpty() && !igniting) {
                ignite()
            }
        }

        fun ignite() {
            lampDisplay.block = Bukkit.createBlockData(Material.COPPER_BULB) // normal copper bulb that will give off more light
            creeper.ignite()
            igniting = true
            ticks = -20
            creeperOwners.remove(creeper)
        }

        fun destroy() {
            creeper.remove()
            lampDisplay.remove()
            glassDisplay.remove()
            creeperOwners.remove(creeper)
            cancel()
        }

        fun explode() {
            val attackerTeamColor = owner.team.dyeColor.color
            val world = creeper.world
            world.spawnParticle(
                Particle.EXPLOSION_EMITTER,
                creeper.location,
                5,
                BLAST_RADIUS / 4,
                BLAST_RADIUS / 4,
                BLAST_RADIUS / 4,
                5.0
            )
            world.spawnParticle(
                Particle.DUST,
                creeper.location,
                1000,
                2.0,
                2.0,
                2.0,
                0.4,
                DustOptions(attackerTeamColor, 5f)
            ) // Red color
            for (entity in world.getNearbyEntitiesByType(Player::class.java, creeper.location, BLAST_RADIUS)) {
                val p = entity as Player
                val lobby = BotBows.getLobby(p) ?: continue
                if (lobby != owner.lobby) continue
                val bp = lobby.getBotBowsPlayer(p) ?: continue
                hitPlayers.add(bp)
            }
            val ability = owner.getAbility(AbilityType.CREEPER_TRAP)
            hitPlayers.forEach {
                it.damage(DamageContext.Player(DamageType.Player.CREEPER, owner))
                if (it !== owner) ability.registerSuccess() else ability.registerFail()
            }
        }
    }

    companion object {
        const val BLOCK_PX: Float = 0.0625f
        protected const val CREEPER_SCALE: Float = 0.75f
        protected const val CREEPER_PX: Float = BLOCK_PX * CREEPER_SCALE

        var ACTIVATION_DELAY: Int = 3
        var BLAST_RADIUS: Double = 4.0

        protected var creeperOwners: MutableMap<Creeper, BotBowsPlayer> = mutableMapOf()

        fun glowCreepers(team: BotBowsTeam, ticks: Int) {
            val creepers: Set<Creeper> = creeperOwners.entries
                .filter { it.value.team == team }
                .map { it.key }
                .toSet()

            creepers.forEach { it.isGlowing = true }
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(),
                Runnable { creepers.forEach { it.isGlowing = false } },
                ticks.toLong()
            )
        }

        fun ignite(creeper: Creeper) {
            if (!creeperOwners.containsKey(creeper)) {
                creeper.ignite()
                return
            }
            val ability = creeperOwners[creeper]!!.getAbility(AbilityType.CREEPER_TRAP) as CreeperTrap
            ability.creeperTicker!!.ignite()
        }
    }
}
