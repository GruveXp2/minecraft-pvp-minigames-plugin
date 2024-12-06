package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HazardMenu extends SettingsMenu {

    private static final Map<String, HazardChance> PERCENT_MAP = new HashMap<>();
    private static final List<String> PERCENT = List.of("DISABLED", "5%", "10%", "25%", "50%", "ALWAYS");

    static {
        PERCENT_MAP.put("DISABLED", HazardChance.DISABLED);
        PERCENT_MAP.put("5%", HazardChance.FIVE);
        PERCENT_MAP.put("10%", HazardChance.TEN);
        PERCENT_MAP.put("25%", HazardChance.TWENTY_FIVE);
        PERCENT_MAP.put("50%", HazardChance.FIFTY);
        PERCENT_MAP.put("ALWAYS", HazardChance.ALWAYS);
    }
    private StormHazard stormHazard;
    private EarthquakeHazard earthquakeHazard;
    private GhostHazard ghostHazard;

    private ItemStack getStormItem() {
        ItemStack item;
        String[] loreDesc = new String[] {"When there is a storm, you will get hit by", "lightning if you stand in dirext exposure", "to the sky for more than 5 seconds"};
        if (stormHazard.getHazardChance() == HazardChance.DISABLED) {
            item = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Storms", ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled",
                    "If enabled, x% of rounds will have storms.", loreDesc[0], loreDesc[1], loreDesc[2]);
        } else {
            item = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Storms", ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled",
                    stormHazard.getHazardChance().getPercent() + "% of rounds will have storms.", loreDesc[0], loreDesc[1], loreDesc[2]);
        }
        return item;
    }

    private ItemStack getEarthquakeItem() {
        ItemStack item;
        String[] loreDesc = new String[] {"When there is an earthwuake, you will get hit by", "stones if you go underground", "for more than 5 seconds"};
        if (earthquakeHazard.getHazardChance() == HazardChance.DISABLED) {
            item = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Earthquakes", ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled",
                    "If enabled, x% of rounds will have earthquakes.", loreDesc[0], loreDesc[1], loreDesc[2]);
        } else {
            item = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Earthquakes", ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled",
                    earthquakeHazard.getHazardChance().getPercent() + "% of rounds will have earthquakes.", loreDesc[0], loreDesc[1], loreDesc[2]);
        }
        return item;
    }

    private ItemStack getGhostItem() {
        ItemStack item;
        String[] loreDesc = new String[] {"When there is ghost mode, you will get haunted", "by your own ghost, and when you touch it,", "you die"};
        if (ghostHazard.getHazardChance() == HazardChance.DISABLED) {
            item = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Haunted Arena", ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled",
                    "If enabled, x% of rounds will be haunted.", loreDesc[0], loreDesc[1], loreDesc[2]);
        } else {
            item = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Haunted Arena", ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled",
                    earthquakeHazard.getHazardChance().getPercent() + "% of rounds will be haunted.", loreDesc[0], loreDesc[1], loreDesc[2]);
        }
        return item;
    }

    @Override
    public String getMenuName() {
        return "Hazards";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case WHITE_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                String s = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                if (e.getSlot() < 9) {
                    if (stormHazard.getHazardChance() != PERCENT_MAP.get(s)) {
                        stormHazard.setHazardChance(PERCENT_MAP.get(s));
                        updateStormBar();
                    }
                } else if (e.getSlot() < 18) {
                    if (earthquakeHazard.getHazardChance() != PERCENT_MAP.get(s)) {
                        earthquakeHazard.setHazardChance(PERCENT_MAP.get(s));
                        updateEarthquakeBar();
                    }
                } else if (e.getSlot() < 27) {
                    if (ghostHazard.getHazardChance() != PERCENT_MAP.get(s)) {
                        ghostHazard.setHazardChance(PERCENT_MAP.get(s));
                        updateGhostBar();
                    }
                }
            } case RED_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> stormHazard.setHazardChance(HazardChance.TEN);
                    case 9 -> earthquakeHazard.setHazardChance(HazardChance.TEN);
                    case 18 -> ghostHazard.setHazardChance(HazardChance.TEN);
                }
            } case LIME_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> stormHazard.setHazardChance(HazardChance.DISABLED);
                    case 9 -> earthquakeHazard.setHazardChance(HazardChance.DISABLED);
                    case 18 -> ghostHazard.setHazardChance(HazardChance.DISABLED);
                }
            }
            case BARRIER -> clicker.closeInventory();
            case FIREWORK_STAR -> {
                if (e.getSlot() == 21) {
                    BotBows.winThresholdMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems();
        stormHazard = settings.stormHazard;
        earthquakeHazard = settings.earthquakeHazard;
        ghostHazard = settings.ghostHazard;
        updateStormBar();
        updateEarthquakeBar();
        updateGhostBar();
        setPageButtons(3, true, false, null);
    }

    void updateStormBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(0, getStormItem());
        inventory.setItem(1, VOID);
        for (int i = 0; i < PERCENT.size(); i++) {
            ItemStack item;
            if (PERCENT_MAP.get(PERCENT.get(i)).getPercent() > stormHazard.getHazardChance().getPercent()) {
                item = makeItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + PERCENT.get(i));
            } else {
                item = makeItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.AQUA + PERCENT.get(i));
            }
            inventory.setItem(i + 2, item);
        }
        inventory.setItem(8, VOID);
    }

    void updateEarthquakeBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(9, getEarthquakeItem());
        inventory.setItem(10, VOID);
        for (int i = 0; i < PERCENT.size(); i++) {
            ItemStack item;
            if (PERCENT_MAP.get(PERCENT.get(i)).getPercent() > earthquakeHazard.getHazardChance().getPercent()) {
                item = makeItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + PERCENT.get(i));
            } else {
                item = makeItem(Material.BROWN_STAINED_GLASS_PANE, ChatColor.GOLD + PERCENT.get(i));
            }
            inventory.setItem(i + 11, item);
        }
        inventory.setItem(17, VOID);
    }

    void updateGhostBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(18, getGhostItem());
        inventory.setItem(19, VOID);
        for (int i = 0; i < PERCENT.size(); i++) {
            ItemStack item;
            if (PERCENT_MAP.get(PERCENT.get(i)).getPercent() > ghostHazard.getHazardChance().getPercent()) {
                item = makeItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + PERCENT.get(i));
            } else {
                item = makeItem(Material.PURPLE_STAINED_GLASS_PANE, ChatColor.GOLD + PERCENT.get(i));
            }
            inventory.setItem(i + 18, item);
        }
        inventory.setItem(26, VOID);
    }
}