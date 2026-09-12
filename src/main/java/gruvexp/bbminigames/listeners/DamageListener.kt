package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.api.damage.DamageContext
import gruvexp.bbminigames.api.damage.DamageType
import gruvexp.bbminigames.extras.StickSlap
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.abilities.CreeperTrap
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBow
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

class DamageListener : Listener {
    @EventHandler
    fun onHit(e: EntityDamageByEntityEvent) {
        if (e.entity is Creeper) {
            val creeper = e.entity as Creeper
            e.damage = 0.01
            CreeperTrap.ignite(creeper)
            return
        }
        if (e.damager is Arrow) {
            val arrow = e.damager as Arrow

            val attacker = arrow.shooter as? LivingEntity ?: return
            val attackerBp: BotBowsPlayer = BotBows.getBotBowsPlayer(attacker.uniqueId) ?: return

            val defender = e.entity as? LivingEntity ?: return
            val defenderBp: BotBowsPlayer = BotBows.getBotBowsPlayer(defender.uniqueId) ?: return

            if (attackerBp.team == defenderBp.team || attacker.isInvulnerable || !defenderBp.lobby.botBowsGame!!.canInteract) {
                arrow.remove() // if the player already was hit and has a cooldown, or if the hit player is of the same team as the attacker, or shooting is disabled, the arrow won't do damage
                e.isCancelled = true
                return
            }
            e.damage = 0.01 // de skal ikke daue men bli satt i spectator til runda er ferdig
            val hasKarmaAura = defenderBp.karmaAura
            if (attackerBp.hasAbilityEquipped(AbilityType.THUNDER_BOW) && (attackerBp.getAbility(AbilityType.THUNDER_BOW) as ThunderBow).isActive) {
                //TODO: effect when hit by thunder bow?
            } else {
                defenderBp.damage(DamageContext.Player(DamageType.Player.BOW, attackerBp))
                attackerBp.obtainWeaponAbilities() // if the player hits, then the weapon ability rule will make the attacker obtain weapon abilities, unless it's the one used to hit
            }
            if (hasKarmaAura) {
                attackerBp.applyKarmaDebuff()
                defenderBp.getAbility(AbilityType.KARMA_POTION).registerSuccess()
            }
        } else {
            val defender = e.entity as? LivingEntity ?: return
            val attacker = e.damager as? Player ?: return // vurder å gjør at det blir as LivingEntity itilfelle det var en BotBowsBot

            val weapon = attacker.inventory.itemInMainHand
            if (weapon.type == Material.STICK) {
                e.damage = 0.01 // basically no dmg but still makes dmg sound
                return
            } else if (weapon.type == Material.BLAZE_ROD) {
                StickSlap.handleHit(attacker)
                e.damage = 0.01 // basically no dmg but still makes dmg sound
                return
            }

            val attackerBp = BotBows.getBotBowsPlayer(attacker)
            val defenderBp = BotBows.getBotBowsPlayer(defender.uniqueId)

            if (attackerBp != null) AbilityListener.onSlap(e, attackerBp, defenderBp, weapon)
            if (defenderBp == null) {
                if (attackerBp != null) {
                    e.isCancelled = true // cant damage ingame players without bow
                }
                e.damage = 0.01
            }
        }
    }

    @EventHandler
    fun onDamaged(e: EntityDamageEvent) {
        if (e.entity is Rabbit) {
            e.isCancelled = true // I have a rabbit pet so this block of code is obvious
            return
        }

        val bp = BotBows.getBotBowsPlayer(e.entity.uniqueId) ?: return
        when (e.cause) {
            DamageCause.SUFFOCATION -> {
                bp.avatar.teleport(bp.location.add(0.0, 1.0, 0.0))
                e.isCancelled = true
            }

            DamageCause.CAMPFIRE, DamageCause.DROWNING, DamageCause.FALL -> e.isCancelled = true

            DamageCause.LAVA -> {
                e.isCancelled = true
                bp.damage(DamageContext.Environment(DamageType.Environment.LAVA))
            }
            else -> {} // add more in the future, like fex fire
        }
    }
}
