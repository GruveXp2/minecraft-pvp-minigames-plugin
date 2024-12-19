package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerMenuRow extends MenuRow{

    public PlayerMenuRow(Inventory inventory, int startSlot, int size) {
        super(inventory, startSlot, size);
    }

    @Override
    public void addItem(ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) throw new IllegalArgumentException("The item must be a player head");
        super.addItem(item);
    }

    public ItemStack getItem(BotBowsPlayer p) {
        for (ItemStack item : itemList) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.getOwningPlayer().getUniqueId().equals(p.player.getUniqueId())) {
                return item;
            }
        }
        return null;
    }
}
