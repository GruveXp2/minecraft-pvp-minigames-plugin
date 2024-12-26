package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class  MapMenu extends SettingsMenu {
    public static final ItemStack CLASSIC_ARENA = makeItem(Material.SLIME_BALL, Component.text("Classic Arena", NamedTextColor.GRAY),
            ChatColor.BLUE + "Blaud" + ChatColor.WHITE + " vs " + ChatColor.RED + "Sauce",
            "A flat arena with modern royal style",
            "Has a huge cave room underground");
    public static final ItemStack ICY_RAVINE = makeItem(Material.SPRUCE_SAPLING, Component.text("Icy Ravine", NamedTextColor.AQUA),
            ChatColor.LIGHT_PURPLE + "Graut" + ChatColor.WHITE + " vs " + ChatColor.GREEN + "Wacky",
            "A flat arena in a spruce forest with ice spikes and igloos",
            "Has a huge ravine in the middle and many caves underground");
    public static final ItemStack ROYAL_CASTLE = makeItem(Material.STONE_BRICK_STAIRS, Component.text("Royal Castle", NamedTextColor.GREEN),
            ChatColor.DARK_AQUA + "Kjødd" + ChatColor.WHITE + " vs " + ChatColor.DARK_GREEN + "Goofy",
            "A castle themed arena",
            ChatColor.YELLOW + "Work in progress");
    public static final ItemStack STEAMPUNK = makeItem(Material.COPPER_BULB, Component.text("Steampunk", NamedTextColor.GOLD),
            ChatColor.GRAY + "Unnamed 0" + ChatColor.WHITE + " vs " + ChatColor.GRAY + "Unnamed 1",
            "A steampunk themed arena",
            ChatColor.YELLOW + "Work in progress");
    public static final ItemStack VOLCANO = makeItem(Material.MAGMA_BLOCK, Component.text("Volcano", NamedTextColor.RED),
            ChatColor.GRAY + "Unnamed 2" + ChatColor.WHITE + " vs " + ChatColor.GRAY + "Unnamed 3",
            "A steampunk themed arena",
            ChatColor.YELLOW + "Work in progress");

    @Override
    public Component getMenuName() {
        return Component.text("Arena map (1/6)");
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (settings.playerIsntMod(BotBows.getBotBowsPlayer(clicker)) && !clickedOnBottomButtons(e)) return;

        switch (e.getCurrentItem().getType()) {
            case SLIME_BALL -> settings.setMap(BotBowsMap.BLAUD_VS_SAUCE);
            case SPRUCE_SAPLING -> settings.setMap(BotBowsMap.GRAUT_VS_WACKY);
            case STONE_BRICK_STAIRS, COPPER_BULB, MAGMA_BLOCK -> clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED));
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 4) {
                    settings.teamsMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems();
        inventory.setItem(2, CLASSIC_ARENA);
        inventory.setItem(3, ICY_RAVINE);
        inventory.setItem(4, ROYAL_CASTLE);
        inventory.setItem(5, STEAMPUNK);
        inventory.setItem(6, VOLCANO);
        setPageButtons(1, false, true);
        setFillerVoid();
    }
}
