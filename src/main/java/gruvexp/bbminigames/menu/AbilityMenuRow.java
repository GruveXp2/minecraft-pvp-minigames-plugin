package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.menu.menus.AbilityMenu;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AbilityMenuRow extends MenuRow {

    private final AbilityMenu menu;

    public AbilityMenuRow(Inventory inventory, int startSlot, int size, AbilityMenu menu) {
        super(inventory, startSlot, size);
        this.menu = menu;
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
            if (itemList.get(i).isSimilar(abilityItem)) {
                int slot = i - firstVisibleItem;
                if (currentPage > 1) slot++;
                return slot;
            }
        }
        return -1;
    }

    protected void goTo(int page) {
        super.goTo(page);
        menu.updateAbilityStatuses();
        for (HumanEntity viewer : inventory.getViewers()) {
            Player p = (Player) viewer;
            menu.handleMenuClose(p);
            menu.open(p);
        }
    }
}
