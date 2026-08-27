package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.api.ability.AbilityContext.*
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnMelee
import gruvexp.bbminigames.api.ability.AbilityTrigger.OnProjectileHit
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.PotionAbility
import gruvexp.bbminigames.twtClassic.ability.abilities.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.WeatherType
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerRiptideEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.cos
import kotlin.math.sin

class AbilityListener : Listener {
    @EventHandler
    fun onProjectileLaunch(e: ProjectileLaunchEvent) {
        val p = e.entity.shooter as? Player ?: return
        val bp: BotBowsPlayer = BotBows.getBotBowsPlayer(p) ?: return

        if (e.entity is Arrow) {
            val arrow = e.entity
            val itemInMainHand: ItemStack = p.inventory.itemInMainHand
            if (AbilityType.fromItem(itemInMainHand) == AbilityType.SPLASH_BOW) {
                (bp.getAbility(AbilityType.SPLASH_BOW) as SplashBow).onLaunch(Launch(arrow))
            } else if (itemInMainHand.type == Material.CROSSBOW) {
                arrow.setGravity(false)
                if (bp.hasAbilityEquipped(AbilityType.THUNDER_BOW)
                    && (bp.getAbility(AbilityType.THUNDER_BOW) as ThunderBow).isActive
                ) {
                    (bp.getAbility(AbilityType.THUNDER_BOW) as ThunderBow).onLaunch(Launch(arrow))
                }
            }
        } else if (e.entity is ThrownPotion) {
            val potion = e.entity as ThrownPotion
            if (potion.item.type == Material.LINGERING_POTION) {
                LingeringPotionTrap.giveRandomEffect(potion)
            }
        }
    }

    @EventHandler
    fun onPotionAbilityUse(e: PlayerItemConsumeEvent) {
        val p = e.player
        val bp = BotBows.getBotBowsPlayer(p) ?: return
        val type = AbilityType.fromItem(e.item) ?: return
        if (!bp.lobby.isGameActive) {
            e.isCancelled = true // kanke bruke abilities i lobbyen
            return
        }
        when (type) {
            AbilityType.BABY_POTION, AbilityType.CHARGE_POTION, AbilityType.KARMA_POTION -> bp.getAbility(type).use()
            else -> error("not a registered potion")
        }
    }

    @EventHandler
    fun onLingeringPotionSplash(e: LingeringPotionSplashEvent) {
        val potion = e.entity
        val thrower = potion.shooter as? Player ?: return
        val throwerBp = BotBows.getBotBowsPlayer(thrower) ?: return

        val ability = throwerBp.getAbility(AbilityType.LINGERING_POTION) as LingeringPotionTrap
        ability.onSplash(e)
    }

    @EventHandler
    fun onCloudApply(e: AreaEffectCloudApplyEvent) {
        val cloud = e.entity
        val cloudOwningBp = LingeringPotionTrap.getCloudOwner(cloud) ?: return

        val ability = cloudOwningBp.getAbility(AbilityType.LINGERING_POTION) as LingeringPotionTrap
        ability.onCloudApply(e)
    }

    @EventHandler
    fun onAbilityDrop(e: PlayerDropItemEvent) {
        val p = e.player
        val bp = BotBows.getBotBowsPlayer(p) ?: return
        val type = AbilityType.fromItem(e.itemDrop.itemStack) ?: return
        if (!bp.hasAbilityEquipped(type)) return  // kan droppe itemet hvis det ikke var equippa

        e.isCancelled = true // kanke droppe ability items
    }

    @EventHandler
    fun onArrowHit(e: ProjectileHitEvent) {
        val projectile = e.entity
        if (projectile !is Arrow) return
        if (projectile.shooter !is Player) return // den som skøyt


        if (projectile.hasMetadata("botbows_ability")) {
            val ability = projectile.getMetadata("botbows_ability").first().value() as? OnProjectileHit ?: return
            ability.onHit(e)
        }
    }

    @EventHandler
    fun onSwing(e: PlayerAnimationEvent) { // left clicking but not hitting anything
        val p = e.player
        val bp = BotBows.getBotBowsPlayer(p) ?: return
        val type = AbilityType.fromItem(p.inventory.itemInMainHand) ?: return
        if (bp.lobby.isGameActive && type == AbilityType.LONG_ARMS) {
            bp.getAbility(AbilityType.LONG_ARMS).use()
        }
    }

    @EventHandler
    fun onRiptide(e: PlayerRiptideEvent) {
        val attacker = e.player
        val attackerBp = BotBows.getBotBowsPlayer(attacker) ?: return
        if (!attackerBp.hasAbilityEquipped(AbilityType.BUBBLE_JET)) return

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
            if (!attackerBp.lobby.botBowsGame!!.stormHazard.isActive) {
                attacker.setPlayerWeather(WeatherType.CLEAR)
            } else {
                attacker.resetPlayerWeather()
            }
        }, 10L)
        attackerBp.getAbility(AbilityType.BUBBLE_JET).use()
    }

    companion object {
        @JvmStatic
        fun onAbilityUse(e: PlayerInteractEvent) {
            val p = e.player
            val bp = BotBows.getBotBowsPlayer(p) ?: return
            val abilityItem = e.item ?: return
            val type = AbilityType.fromItem(abilityItem) ?: return

            if (!bp.lobby.isGameActive && !TestCommand.testAbilities || !bp.lobby.botBowsGame!!.canInteract) {
                e.isCancelled = true // kanke bruke abilities i lobbyen
                return
            }
            when (type) {
                AbilityType.ENDER_PEARL, AbilityType.RADAR, AbilityType.THUNDER_BOW, AbilityType.SALMON_SLAP, AbilityType.LINGERING_POTION -> bp.getAbility(type).use()

                AbilityType.BUBBLE_JET -> {
                    p.resetPlayerWeather()
                    p.inventory.itemInMainHand.addEnchantment(Enchantment.RIPTIDE, 3)
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
                        if (!bp.lobby.botBowsGame!!.stormHazard.isActive) {
                            p.setPlayerWeather(WeatherType.CLEAR)
                        } else {
                            p.resetPlayerWeather()
                        }
                    }, 60L)
                }

                AbilityType.CREEPER_TRAP -> {
                    val clickedBlock = e.clickedBlock ?: return
                    val face = e.blockFace
                    var spawnBlock = clickedBlock.getRelative(face)
                    if (spawnBlock.getRelative(BlockFace.UP).type.isSolid()) return

                    while (!spawnBlock.getRelative(BlockFace.DOWN).type.isSolid()) {
                        spawnBlock = spawnBlock.getRelative(BlockFace.DOWN)
                    }
                    val placeLoc = spawnBlock.location.add(0.5, 0.0, 0.5)

                    (bp.getAbility(type) as CreeperTrap).trigger(EntityPlace(placeLoc))
                }

                AbilityType.LASER_TRAP -> {
                    val clickedBlock = e.clickedBlock ?: return
                    val face = e.blockFace
                    val spawnBlock = clickedBlock.getRelative(face)
                    if (spawnBlock.type != Material.AIR) {
                        e.isCancelled = true
                        return
                    }
                    (bp.getAbility(type) as LaserTrap).onPlace(BlockPlace(spawnBlock, face))
                }

                else -> {
                    if (type.category == AbilityCategory.POTION) {
                        val particleCount = 200
                        val radius = PotionAbility.RADIUS
                        val loc = p.location.add(0.0, 0.1, 0.0)
                        val y = loc.y
                        var i = 0
                        while (i < particleCount) {
                            val θ = 2 * Math.PI * i / particleCount
                            val x = loc.x + radius * cos(θ)
                            val z = loc.z + radius * sin(θ)

                            val particleLoc = Location(loc.world, x, y, z)
                            p.world.spawnParticle(
                                Particle.DUST,
                                particleLoc,
                                1,
                                0.0,
                                0.0,
                                0.0,
                                0.4,
                                Particle.DustOptions(bp.team.dyeColor.color, 2.5f)
                            )
                            i++
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun onSlap(
            e: EntityDamageByEntityEvent,
            attackerBp: BotBowsPlayer,
            defenderBp: BotBowsPlayer?,
            weapon: ItemStack
        ) {
            if (defenderBp == null) return
            if (attackerBp.team == defenderBp.team) {
                e.isCancelled = true
                return
            }
            val type = AbilityType.fromItem(weapon) ?: return
            val ability = attackerBp.getAbility(type)
            if (ability is OnMelee) {
                ability.trigger(Melee(defenderBp))
            }
            e.isCancelled = true
        }
    }
}
