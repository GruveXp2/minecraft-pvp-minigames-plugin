package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.scheduler.BukkitRunnable;

public class FloatSpellAbility extends Ability {

    public static final int DURATION = 3;
    private boolean immune = false;

    public FloatSpellAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.ENDER_PEARL;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    public void use() {
        super.use();
        immune = true;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::deImmunize, 20L * DURATION);
    }

    private void deImmunize() {
        this.immune = false;
    }

    public boolean isImmune() {
        return immune;
    }

    public static void animateChicken(Chicken chicken) {
        new BukkitRunnable() {
            int i = 1;
            final int TOTAL = DURATION * 20;
            @Override
            public void run() {
                if (i == TOTAL) {
                    this.cancel();
                    chicken.remove();
                }
                double angle = i * 5 * Math.PI / 600;
                chicken.getAttribute(Attribute.SCALE).setBaseValue(2 * Math.sin(Math.PI / 6 + angle));
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 100L, 1L);
    }
}
