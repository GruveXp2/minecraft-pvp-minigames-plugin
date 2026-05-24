package gruvexp.bbminigames.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Menu implements InventoryHolder {

    public static final NamespacedKey ACTION_KEY = new NamespacedKey("botbows", "menu_action");
    public static final ItemStack DISABLED_SLOT = makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
    public static final ItemStack VOID = makeItem("void", Component.empty());

    protected Inventory inventory;

    //The owner of the inventory created is the Menu itself,
    // so we are able to reverse engineer the Menu object from the
    // inventoryHolder in the MenuListener class when handling clicks
    public Menu() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        setFillerVoid();
    }

    // name at the top of the inventory
    public abstract Component getMenuName();

    // how many slots in the menu, must be 9n
    public abstract int getSlots();

    // what happens when clicking in the menu
    public abstract void handleMenu(InventoryClickEvent e);

    public boolean handlesEmptySlots() {
        return false; // By default, menus don't handle empty slots
    }

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

    public static ItemStack makeItem(Material material, TextComponent displayName, String actionId, Component... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
        itemMeta.lore(List.of(lore));

        if (actionId != null) {
            itemMeta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, actionId);
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack makeItem(Material material, TextComponent displayName, Component... lore) {
        return makeItem(material, displayName, null, lore);
    }

    public static ItemStack makeItem(Material material, TextComponent displayName, String actionId) {
        return makeItem(material, displayName, actionId, new Component[0]);
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
        return makeItem(Material.FIREWORK_STAR, customModelData, displayName.decoration(TextDecoration.ITALIC, false), null, null, lore);
    }

    public static ItemStack makeItem(String customModelData, TextComponent displayName, String actionId, Component... lore) {
        return makeItem(Material.FIREWORK_STAR, customModelData, displayName.decoration(TextDecoration.ITALIC, false), null, actionId, lore);
    }

    public static ItemStack makeItem(String customModelData, TextComponent displayName, NamespacedKey actionKey, String actionId, Component... lore) {
        return makeItem(Material.FIREWORK_STAR, customModelData, displayName.decoration(TextDecoration.ITALIC, false), actionKey, actionId, lore);
    }

    public static ItemStack makeItem(Material material, String customModelData, TextComponent displayName, Component... lore) {
        return makeItem(material, customModelData, displayName.decoration(TextDecoration.ITALIC, false), null, null, lore);
    }

    public static ItemStack makeItem(Material material, String customModelData, TextComponent displayName, NamespacedKey actionKey, String actionId, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
        itemMeta.lore(List.of(lore));

        CustomModelDataComponent customModelDataComponent = itemMeta.getCustomModelDataComponent();
        customModelDataComponent.setStrings(List.of(customModelData));
        itemMeta.setCustomModelDataComponent(customModelDataComponent);

        if (actionId != null) {
            itemMeta.getPersistentDataContainer().set(actionKey != null ? actionKey : ACTION_KEY, PersistentDataType.STRING, actionId);
        }

        item.setItemMeta(itemMeta);
        return item;
    }
}

