package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.menu.menus.AbilityMenu;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AbilityMenuRow extends MenuRow {

    private final AbilityMenu menu;

    public AbilityMenuRow(Inventory inventory, String menuActionId, int startSlot, int size, AbilityMenu menu) {
        super(inventory, menuActionId, startSlot, size);
        this.menu = menu;
        initItems();
    }

    public void initItems() {
        for (AbilityType type : AbilityType.values()) {
            addItem(type.getAbilityItem().clone());
        }
    }

    public Integer getAbilitySlot(AbilityType type) {
        for (int i = 0; i < itemList.size(); i++) {
            if (AbilityType.fromItem(itemList.get(i)) == type) {
                int slot = i - firstVisibleItem;
                if (currentPage > 1) slot++;
                return slot;
            }
        }
        return null;
    }

    protected void goTo(int page) {
        super.goTo(page);
        menu.updateAbilityStatuses();
        List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
        for (HumanEntity viewer : viewers) {
            Player p = (Player) viewer;
            menu.handleMenuClose(p);
            menu.open(p);
        }
    }
}
