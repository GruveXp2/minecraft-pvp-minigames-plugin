package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.CreeperTrapAbility;
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBowAbility;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
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
            CreeperTrapAbility.ignite(creeper);
            return;
        }
        if ((e.getDamager() instanceof Arrow arrow)) {
            if (!(arrow.getShooter() instanceof Player attacker)) {return;}
            if (e.getEntity() instanceof Player defender) {
                if (!BotBows.isPlayerJoined(attacker) || !BotBows.isPlayerJoined(defender)) {return;} // hvis de ikke er i gamet
                arrow.setKnockbackStrength(8);
                BotBowsPlayer attackerBp = BotBows.getLobby(attacker).getBotBowsPlayer(attacker);
                BotBowsPlayer defenderBp = BotBows.getLobby(defender).getBotBowsPlayer(defender);

                if (AbilityListener.thunderArrows.containsKey(arrow)) {
                    AbilityListener.thunderArrows.get(arrow).cancel();
                    AbilityListener.thunderArrows.remove(arrow);
                }
                if (attackerBp.getTeam() == defenderBp.getTeam() || attacker.isGlowing() || !defenderBp.lobby.botBowsGame.canShoot) {
                    arrow.remove(); // if the player already was hit and has a cooldown, or if the hit player is of the same team as the attacker, or shooting is disabled, the arrow won't do damage
                    e.setCancelled(true);
                    return;
                }
                e.setDamage(0.01); // de skal ikke daue men bli satt i spectator til runda er ferdig
                boolean hasKarma = defenderBp.hasKarmaEffect();
                if (attackerBp.hasAbilityEquipped(AbilityType.THUNDER_BOW) && ((ThunderBowAbility) attackerBp.getAbility(AbilityType.THUNDER_BOW)).isActive()) {
                    ThunderBowAbility.handleArrowHitPlayer(attackerBp, defenderBp);
                } else {
                    defenderBp.handleHit(Component.text(" was sniped by "), attackerBp);
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
                    e.setCancelled(true); // hvis man bruker stick så skjer det ikke noe
                    return;
                } else if (weapon.getType() == Material.BLAZE_ROD) {
                    StickSlap.handleHit(attacker);
                    e.setDamage(0.01); // gjør ikke damage men lager fortsatt damage lyd
                    return;
                } else {
                    AbilityListener.onSlap(e, attacker, defender, weapon);
                }
            }
            if (!BotBows.isPlayerJoined(defender)) {
                e.setCancelled(true); // cant damage ingame players without bow
            }// entitien som utførte damag
        }
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && (e.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION)) {
            Lobby lobby = BotBows.getLobby(p);
            if (lobby == null) return;
            p.teleport(p.getLocation().add(0, 1, 0));
            e.setCancelled(true);
        } else if (e.getCause() == EntityDamageEvent.DamageCause.CAMPFIRE ||
                e.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            e.setCancelled(true);
        }
    }
}
