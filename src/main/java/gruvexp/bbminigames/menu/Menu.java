package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Objects;

public abstract class Menu implements InventoryHolder {

    //Protected values that can be accessed in the menus
    protected Inventory inventory;
    protected final ItemStack VOID = getVoidItem();
    protected final ItemStack LEFT = getPrevPageItem();
    protected final ItemStack CLOSE = makeItem(Material.BARRIER, ChatColor.RED + "Close menu");
    protected final ItemStack RIGHT = getNextPageItem();

    //The owner of the inventory created is the Menu itself,
    // so we are able to reverse engineer the Menu object from the
    // inventoryHolder in the MenuListener class when handling clicks
    public Menu() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();
    }

    //let each menu decide their name
    public abstract String getMenuName();

    //let each menu decide their slot amount
    public abstract int getSlots();

    //let each menu decide how the items in the menu will be handled when clicked
    public abstract void handleMenu(InventoryClickEvent e);

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    //When called, an inventory is created and opened for the player
    public void open(Player p) {
        p.openInventory(inventory);
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    //Helpful utility method to fill all remaining slots with "filler glass"
    public void setFillerVoid(){
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, VOID);
            }
        }
    }

    public static ItemStack makeItem(Material material, String displayName, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(Arrays.asList(lore));

        item.setItemMeta(itemMeta);
        return item;
    }

    private static ItemStack getVoidItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(""));
        meta.setCustomModelData(77000); // void texture
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getNextPageItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Next page"));
        meta.setCustomModelData(77001); // void texture
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getPrevPageItem() {
        ItemStack item = new ItemStack(Material.FIREWORK_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Prev page"));
        meta.setCustomModelData(77002); // void texture
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack makeHeadItem(Player p, ChatColor teamColor) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName(teamColor + p.getPlayerListName());
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING, p.getUniqueId().toString());
        itemMeta.setOwningPlayer(Bukkit.getPlayer(p.getName()));

        item.setItemMeta(itemMeta);
        return item;
    }

    protected void setPageButtons(int rowIndex, boolean prevMenuButton, boolean nextMenuButton, ItemStack moreSettingsButton) {
        inventory.setItem(rowIndex*9    , VOID);
        inventory.setItem(rowIndex*9 + 1, VOID);
        inventory.setItem(rowIndex*9 + 2, VOID);
        inventory.setItem(rowIndex * 9 + 3, prevMenuButton ? getPrevPageItem() : VOID);
        inventory.setItem(rowIndex * 9 + 4, Objects.requireNonNullElse(moreSettingsButton, VOID));
        inventory.setItem(rowIndex * 9 + 5, nextMenuButton ? getNextPageItem() : VOID);
        inventory.setItem(rowIndex*9 + 6, VOID);
        inventory.setItem(rowIndex*9 + 7, VOID);
        inventory.setItem(rowIndex*9 + 8, VOID);
    }
}

