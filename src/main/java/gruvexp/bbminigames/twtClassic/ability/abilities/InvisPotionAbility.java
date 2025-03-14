package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InvisPotionAbility extends Ability {

    public InvisPotionAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.INVIS_POTION);
    }

    @Override
    public void use() {
        super.use();
        PlayerInventory inv = bp.player.getInventory();
        inv.setItem(9, inv.getHelmet());
        inv.setItem(10, inv.getChestplate());
        inv.setItem(11, inv.getLeggings());
        inv.setItem(12, inv.getBoots());
        inv.setArmorContents(new ItemStack[] {null, null, null, null});
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), task -> {
            for (int i = 0; i < 4; i++) { // flytter armor ned
                inv.setItem(9 + i, null);
            }
            inv.setArmorContents(new ItemStack[] {inv.getItem(9), inv.getItem(10), inv.getItem(11), inv.getItem(12)});
        }, 100L); // 5 sekunder
    }
}
