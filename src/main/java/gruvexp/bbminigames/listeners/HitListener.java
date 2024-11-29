package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.twtClassic.BotBows;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HitListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if ((e.getDamager() instanceof Arrow arrow)) {
            if (!(arrow.getShooter() instanceof Player attacker)) {return;} // den som skøyt
            if (!(e.getEntity() instanceof Player defender)) {return;} // den som blei hitta
            if (!BotBows.settings.isPlayerJoined(attacker) || !BotBows.settings.isPlayerJoined(defender)) {return;} // hvis de ikke er i gamet
            arrow.setKnockbackStrength(8);
            if (BotBows.getBotBowsPlayer(attacker).getTeam() == BotBows.getBotBowsPlayer(defender).getTeam() || attacker.isGlowing()) {
                arrow.remove(); // if the player already was hit and has a cooldown, or if the hit player is of the same team as the attacker, the arrow won't do damage
                e.setCancelled(true);
                return;
            }
            e.setDamage(0.01); // de skal ikke daue men bli satt i spectator til runda er ferig
            BotBows.getBotBowsPlayer(defender).handleHit(BotBows.getBotBowsPlayer(attacker));
        } else {
            if (!(e.getEntity() instanceof Player defender)) {return;} // den som blei hitta
            if (e.getDamager() instanceof Player attacker) {
                if (attacker.getInventory().getItemInMainHand().getType() == Material.STICK) {
                    e.setCancelled(true); // hvis man bruker stick så skjer det ikke noe
                    return;
                }
                StickSlap.handleHit(attacker);
                e.setDamage(0.01); // gjør ikke damage men lager fortsatt damage lyd
                return;
            }
            if (BotBows.settings.isPlayerJoined(defender)) {
                e.setCancelled(true); // cant damage ingame players without bow
            }// entitien som utførte damag
        }
    }
}
