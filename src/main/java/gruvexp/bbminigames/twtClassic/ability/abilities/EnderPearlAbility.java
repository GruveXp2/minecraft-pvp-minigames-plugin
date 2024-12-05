package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EnderPearlAbility extends Ability {
    protected EnderPearlAbility(BotBowsPlayer player) {
        super(player);
        this.abilityItem = new ItemStack(Material.ENDER_PEARL);
        this.cooldownItem = new ItemStack(Material.CYAN_CONCRETE);
        this.maxCooldown = 15;
    }
}
