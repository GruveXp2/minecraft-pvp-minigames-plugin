package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class HealthMenu extends SettingsMenu {

    private static final ItemStack DYNAMIC_POINTS_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Dynamic points",
            ChatColor.DARK_RED + "Disabled", "If enabled, winning team gets 1", "point for each remaining hp.", "If disbabled, winning team only gets 1 point.");
    private static final ItemStack DYNAMIC_POINTS_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Dynamic points",
            ChatColor.DARK_GREEN + "Enabled", "If enabled, winning team gets 1", "point for each remaining hp.", "If disbabled, winning team only gets 1 point.");

    private static final ItemStack CUSTOM_HP_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Custom player HP",
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled", "By enabling this, each player", "can have a different amount of hp");
    private static final ItemStack CUSTOM_HP_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Custom player HP",
            ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled", "By enabling this, each player", "can have a different amount of hp");

    private boolean customHP;
    private MenuSlider healthSlider;

    @Override
    public String getMenuName() {
        return "Select player health";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // if you click on a player then they change teams
        Player clicker = (Player) e.getWhoClicked();

        switch (e.getCurrentItem().getType()) { // stuff som skal gjøres når man trykker på et item
            case WHITE_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE -> {
                settings.setMaxHP(Integer.parseInt(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())));
                updateMenu();
            }
            case RED_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(DYNAMIC_POINTS_DISABLED)) {
                    enableDynamicPoints();
                } else if (e.getCurrentItem().equals(CUSTOM_HP_DISABLED)) { // clicked on custom hp setting
                    enableCustomHP();
                }
            }
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(DYNAMIC_POINTS_ENABLED)) {
                    disableDynamicPoints();
                } else if (e.getCurrentItem().equals(CUSTOM_HP_ENABLED)) { // clicked on custom hp setting
                    disableCustomHP();
                }
            }
            case PLAYER_HEAD -> {
                ItemStack head = e.getCurrentItem();
                Player p = Bukkit.getPlayer(UUID.fromString(head.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING)));
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
                int slot = e.getSlot();
                int maxHP = head.getAmount();

                if (maxHP > 9) {
                    maxHP += 5;
                    if (maxHP > 20) {
                        maxHP = 1;
                    }
                } else {
                    maxHP += 1;
                }

                head.setAmount(maxHP);
                inventory.setItem(slot, head);
                bp.setMaxHP(maxHP);
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.teamsMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.winThresholdMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems(); // initer settings
        healthSlider = new MenuSlider(inventory, 11, Material.PINK_STAINED_GLASS_PANE, NamedTextColor.RED, List.of("1", "2", "3", "4", "5"));
        setPageButtons(2, true, true, null);
        setFillerVoid();
    }

    public void updateMenu() {
        if (customHP) { // each player can have their own health
            for (int i = 9; i < 18; i++) {
                inventory.setItem(i, null);
            }
            for (int i = 0; i < settings.team1.size(); i++) {
                BotBowsPlayer p = settings.team1.getPlayer(i);
                ItemStack item = makeHeadItem(p.PLAYER, settings.team1.COLOR);
                item.setAmount(p.getMaxHP());
                inventory.setItem(i + 9, item);
            }
            for (int i = 0; i < settings.team2.size(); i++) {
                BotBowsPlayer p = settings.team2.getPlayer(i);
                ItemStack item = makeHeadItem(p.PLAYER, settings.team2.COLOR);
                item.setAmount(p.getMaxHP());
                inventory.setItem(17 - i, item);
            }
        } else { // The normal menu with a slider
            healthSlider.setProgressSlots(settings.getMaxHP());
        }
    }
    
    public void enableCustomHP() {
        customHP = true;
        inventory.setItem(6, CUSTOM_HP_ENABLED);
        inventory.setItem(13, VOID);
        updateMenu();
    }

    public void disableCustomHP() {
        customHP = false;
        inventory.setItem(6, CUSTOM_HP_DISABLED);
        inventory.setItem(9, VOID);
        inventory.setItem(10, VOID);
        inventory.setItem(16, VOID);
        inventory.setItem(17, VOID);
        settings.setMaxHP(3);
    }

    public void enableDynamicPoints() {
        inventory.setItem(2, DYNAMIC_POINTS_ENABLED);
        settings.setDynamicScoring(true);
    }

    public void disableDynamicPoints() {
        inventory.setItem(2, DYNAMIC_POINTS_DISABLED);
        settings.setDynamicScoring(false);
    }
}
