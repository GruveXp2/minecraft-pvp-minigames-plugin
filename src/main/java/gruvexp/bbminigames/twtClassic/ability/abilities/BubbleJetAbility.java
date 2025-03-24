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

    @Override
    public void use() {
        super.use();
        Player p = bp.player;
        p.setInvulnerable(true);
        if (riptideTask != null) return;
        riptideTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isOnGround() || p.isSwimming()) {
                    p.setInvulnerable(false);
                    this.cancel(); // if the player is done riptiding and hitting the ground
                    riptideTask = null;
                    return;
                }
                for (Entity entity : Main.WORLD.getNearbyEntities(p.getLocation(), DAMAGE_RADIUS, DAMAGE_RADIUS, DAMAGE_RADIUS, entity -> entity instanceof Player)) {
                    Player defender = (Player) entity;
                    if (defender == p) continue;
                    Lobby lobby = BotBows.getLobby(defender);
                    if (lobby == null) return;
                    if (lobby != BotBows.getLobby(p)) return;
                    defender.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false));
                    lobby.getBotBowsPlayer(defender).handleHit(Component.text(" was hit by bubble jet from "), lobby.getBotBowsPlayer(p));
                }
            }
        };
        riptideTask.runTaskTimer(Main.getPlugin(), 0L, 2L);
    }
}
