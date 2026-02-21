package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BubbleJet extends Ability {

    public static final double DAMAGE_RADIUS = 2.0;

    BukkitRunnable riptideTask;

    public BubbleJet(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.BUBBLE_JET);
    }

    @Override
    public void use() {
        super.use();
        bp.setInvulnerable(true);
        if (riptideTask != null) return;
        riptideTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (bp.isOnGround()) {
                    bp.setInvulnerable(false);
                    this.cancel(); // if the player is done riptiding and hitting the ground
                    riptideTask = null;
                    return;
                }
                bp.getNearbyPlayers(DAMAGE_RADIUS).stream()
                        .filter(p -> p.getTeam() != bp.getTeam())
                        .forEach(target -> {
                            target.avatar.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 1, true, false));
                            target.handleHit(Component.text(" was hit by bubble jet from "), bp);
                        });
            }
        };
        riptideTask.runTaskTimer(Main.getPlugin(), 0L, 2L);
    }
}
