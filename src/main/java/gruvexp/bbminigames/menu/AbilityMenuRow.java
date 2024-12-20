package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AbilityMenuRow extends MenuRow{

    public AbilityMenuRow(Inventory inventory, int startSlot, int size) {
        super(inventory, startSlot, size);
        initItems();
    }

    public void initItems() {
        for (AbilityType type : AbilityType.values()) {
            itemList.add(type.getAbilityItem());
        }
    }

    public int getAbilitySlot(AbilityType type) {
        ItemStack abilityItem = type.getAbilityItem();
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).isSimilar(abilityItem)) return i;
        }
        return -1;
    }
}
