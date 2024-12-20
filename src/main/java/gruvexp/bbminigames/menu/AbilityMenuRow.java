package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.inventory.Inventory;

public class AbilityMenuRow extends MenuRow{

    public AbilityMenuRow(Inventory inventory, int startSlot, int size) {
        super(inventory, startSlot, size);
    }

    public void initItems() {
        for (AbilityType type : AbilityType.values()) {
            itemList.add(type.getAbilityItem());
        }
    }
}
