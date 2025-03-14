package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BubbleJetAbility extends Ability {

    public static final double DAMAGE_RADIUS = 2.0;

    BukkitRunnable riptideTask;

    public BubbleJetAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.BUBBLE_JET);
    }

    public void use(Player attacker) {
        super.use();
        attacker.setInvulnerable(true);
        if (riptideTask == null) {
            riptideTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (attacker.isOnGround() || attacker.isSwimming()) {
                        this.cancel(); // if the player is done riptiding and hitting the ground
                        attacker.setInvulnerable(false);
                        return;
                    }
                    for (Entity entity : Main.WORLD.getNearbyEntities(attacker.getLocation(), DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS, entity -> entity instanceof Player)) {
                        Player defender = (Player) entity;
                        Lobby lobby = BotBows.getLobby(defender);
                        if (lobby == null) return;
                        if (lobby != BotBows.getLobby(attacker)) return;
                        defender.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false));
                        lobby.getBotBowsPlayer(defender).handleHit(lobby.getBotBowsPlayer(attacker), Component.text(" was hit by bubble jet from "));
                    }
                }
            };
        }
        riptideTask.runTaskTimer(Main.getPlugin(), 0L, 2L);
    }
}
