package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

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
            NamespacedKey key = new NamespacedKey(Main.getPlugin(), "uuid");
            String storedUUID = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (storedUUID.equals(p.avatar.getUUID().toString())) {
                return item;
            }
        }
        return null;
    }
}
