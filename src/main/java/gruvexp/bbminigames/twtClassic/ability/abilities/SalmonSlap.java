package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SalmonSlap extends Ability implements AbilityTrigger.OnMelee {

    public static final int DURATION = 5; // seconds
    public static final ItemStack SALMON = new ItemStack(Material.SALMON);

    public SalmonSlap(BotBowsPlayer bp, int slot) {
        super(bp, slot, AbilityType.SALMON_SLAP);
    }

    @Override
    public void use() {
        bp.player.getInventory().setItem(getHotBarSlot(), SALMON);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), super::use, 20 * DURATION);
    }

    @Override
    public void trigger(AbilityContext.Melee ctx) {
        ctx.defender().handleHit(Component.text(" was slapped by "), bp);
    }
}
