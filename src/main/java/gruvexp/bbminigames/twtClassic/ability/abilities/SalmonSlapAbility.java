package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SalmonSlapAbility extends Ability {

    public static final int DURATION = 5; // seconds
    public static final ItemStack SALMON = new ItemStack(Material.SALMON);

    public SalmonSlapAbility(BotBowsPlayer bp, int slot) {
        super(bp, slot);
        this.type = AbilityType.SALMON_SLAP;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    public void use() {
        bp.player.getInventory().setItem(getHotBarSlot(), SALMON);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), super::use, 20 * DURATION);
    }
}
