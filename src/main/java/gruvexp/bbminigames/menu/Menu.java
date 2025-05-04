package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Menu implements InventoryHolder {

    //Protected values that can be accessed in the menus
    protected Inventory inventory;
    public static final ItemStack VOID = makeItem("void", Component.empty());
    public static final ItemStack PREV = makeItem("prev", Component.text("Prev"));
    public static final ItemStack NEXT = makeItem("next", Component.text("Next"));
    public static final ItemStack DISABLED = makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty());

    //The owner of the inventory created is the Menu itself,
    // so we are able to reverse engineer the Menu object from the
    // inventoryHolder in the MenuListener class when handling clicks
    public Menu() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();
    }

    //let each menu decide their name
    public abstract Component getMenuName();

    //let each menu decide their slot amount
    public abstract int getSlots();

    //let each menu decide how the items in the menu will be handled when clicked
    public abstract void handleMenu(InventoryClickEvent e);

    public boolean handlesEmptySlots() {
        return false; // By default, menus don't handle empty slots
    }

    protected boolean clickedOnBottomButtons(InventoryClickEvent e) {
        return e.getSlot() > getSlots() - 9;
    }

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    //When called, an inventory is created and opened for the player
    public void open(Player p) {
        p.openInventory(inventory);
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public @NotNull Inventory getInventory() {
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

    public static ItemStack makeItem(Material material, TextComponent displayName, Component... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
        itemMeta.lore(List.of(lore));

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack makeItem(Material material, TextComponent displayName, int amount) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(itemMeta);
        item.setAmount(amount);
        return item;
    }

    public static ItemStack makeItem(String customModelData, TextComponent displayName, Component... lore) {
        return makeItem(Material.FIREWORK_STAR, customModelData, displayName.decoration(TextDecoration.ITALIC, false), lore);
    }

    public static ItemStack makeItem(Material material, String customModelData, TextComponent displayName, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
        itemMeta.lore(List.of(lore));

        CustomModelDataComponent customModelDataComponent = itemMeta.getCustomModelDataComponent();
        customModelDataComponent.setStrings(List.of(customModelData));
        itemMeta.setCustomModelDataComponent(customModelDataComponent);

        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack makeHeadItem(Player p, TextColor teamColor) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.displayName(Component.text(p.getName(), teamColor).decoration(TextDecoration.ITALIC, false));
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING, p.getUniqueId().toString());
        itemMeta.setOwningPlayer(Bukkit.getPlayer(p.getName()));

        item.setItemMeta(itemMeta);
        return item;
    }

    protected void setPageButtons(int rowIndex, boolean prevMenuButton, boolean nextMenuButton) {
        inventory.setItem(rowIndex*9    , VOID);
        inventory.setItem(rowIndex*9 + 1, VOID);
        inventory.setItem(rowIndex*9 + 2, VOID);
        inventory.setItem(rowIndex*9 + 3, prevMenuButton ? PREV : VOID);
        inventory.setItem(rowIndex*9 + 4, VOID);
        inventory.setItem(rowIndex*9 + 5, nextMenuButton ? NEXT : VOID);
        inventory.setItem(rowIndex*9 + 6, VOID);
        inventory.setItem(rowIndex*9 + 7, VOID);
        inventory.setItem(rowIndex*9 + 8, VOID);
    }
}

