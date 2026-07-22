package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.api.damage.DamageContext;
import gruvexp.bbminigames.api.damage.DamageType;
import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.CreeperTrap;
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBow;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Creeper creeper) {
            e.setDamage(0.01);
            CreeperTrap.ignite(creeper);
            return;
        }
        if ((e.getDamager() instanceof Arrow arrow)) {
            if (!(arrow.getShooter() instanceof LivingEntity attacker)) {return;}
            BotBowsPlayer attackerBp = BotBows.getBotBowsPlayer(attacker.getUniqueId());
            if (e.getEntity() instanceof LivingEntity defender) {
                BotBowsPlayer defenderBp = BotBows.getBotBowsPlayer(defender.getUniqueId());
                if (attackerBp == null || defenderBp == null) return;

                if (attackerBp.getTeam() == defenderBp.getTeam() || attacker.isInvulnerable() || !defenderBp.lobby.botBowsGame.canInteract) {
                    arrow.remove(); // if the player already was hit and has a cooldown, or if the hit player is of the same team as the attacker, or shooting is disabled, the arrow won't do damage
                    e.setCancelled(true);
                    return;
                }
                e.setDamage(0.01); // de skal ikke daue men bli satt i spectator til runda er ferdig
                boolean hasKarma = defenderBp.hasKarmaEffect();
                if (attackerBp.hasAbilityEquipped(AbilityType.THUNDER_BOW) && ((ThunderBow) attackerBp.getAbility(AbilityType.THUNDER_BOW)).isActive()) {
                } else {
                    //defenderBp.handleHit(Component.text(" was sniped by "), attackerBp);
                    defenderBp.damage(new DamageContext.Player(DamageType.Player.BOW, attackerBp));
                    attackerBp.obtainWeaponAbilities(); // if the player hits, then the weapon ability rule will make the attacker obtain weapon abilities, unless it's the one used to hit
                }
                if (hasKarma) {
                    attackerBp.getKarma();
                }
            }
        } else {
            if (!(e.getEntity() instanceof Player defender)) {return;}
            if (e.getDamager() instanceof Player attacker) {
                ItemStack weapon = attacker.getInventory().getItemInMainHand();
                if (weapon.getType() == Material.STICK) {
                    e.setDamage(0.01); // gjør ikke damage men lager fortsatt damage lyd
                    return;
                } else if (weapon.getType() == Material.BLAZE_ROD) {
                    StickSlap.handleHit(attacker);
                    e.setDamage(0.01); // gjør ikke damage men lager fortsatt damage lyd
                    return;
                } else {
                    if (BotBows.isPlayerJoined(attacker)) AbilityListener.onSlap(e, BotBows.getBotBowsPlayer(attacker), BotBows.getBotBowsPlayer(defender), weapon);
                }
            }
            if (!BotBows.isPlayerJoined(defender)) {
                if (e.getDamager() instanceof Player attacker && BotBows.isPlayerJoined(attacker)) {
                    e.setCancelled(true); // cant damage ingame players without bow
                }
                e.setDamage(0.01);
            }// entitien som utførte damag
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Rabbit) e.setCancelled(true); // I have a rabbit pet so this is obvious
        BotBowsPlayer bp = BotBows.getBotBowsPlayer(entity.getUniqueId());
        if (bp == null) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            entity.teleport(entity.getLocation().add(0, 1, 0));
            e.setCancelled(true);
        } else if (e.getCause() == EntityDamageEvent.DamageCause.CAMPFIRE ||
                e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            e.setCancelled(true);
        } else if (e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            e.setCancelled(true);
            bp.damage(new DamageContext.Environment(DamageType.Environment.LAVA));
        }
    }
}
