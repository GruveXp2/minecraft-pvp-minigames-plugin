package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class InvisPotionAbility extends Ability {
    public InvisPotionAbility(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.INVIS_POTION;
        this.baseCooldown = 25;
    }

    @Override
    public void use() {
        super.use();
        Inventory inv = player.player.getInventory();
        for (int i = 0; i < 4; i++) { // flytter armor ned
            inv.setItem(9 + i, inv.getItem(100 + i));
            inv.setItem(100 + i, null);
        }
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), task -> {
            for (int i = 0; i < 4; i++) { // flytter armor ned
                inv.setItem(100 + i, inv.getItem(9 + i));
                inv.setItem(9 + i, null);
            }
        }, 100L); // 5 sekunder
    }
}
