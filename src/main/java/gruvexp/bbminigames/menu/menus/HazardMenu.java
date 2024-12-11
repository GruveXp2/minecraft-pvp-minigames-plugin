package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HazardMenu extends SettingsMenu {

    private static final List<String> PERCENT = HazardChance.getPercentStrings();
    private StormHazard stormHazard;
    private EarthquakeHazard earthquakeHazard;
    private GhostHazard ghostHazard;

    private MenuSlider stormSlider;
    private MenuSlider earthQuakeSlider;
    private MenuSlider ghostSlider;
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
                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                HazardChance chance = HazardChance.of(s);
                if (e.getSlot() < 9) {
                    if (stormHazard.getHazardChance() != chance) {
                        stormHazard.setHazardChance(chance);
                        updateStormBar();
                    }
                } else if (e.getSlot() < 18) {
                    if (earthquakeHazard.getHazardChance() != chance) {
                        earthquakeHazard.setHazardChance(chance);
                        updateEarthquakeBar();
                    }
                } else if (e.getSlot() < 27) {
                    if (ghostHazard.getHazardChance() != chance) {
                        ghostHazard.setHazardChance(chance);
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
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.winThresholdMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.abilityMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems();
        stormHazard = settings.stormHazard;
        stormSlider = new MenuSlider(inventory, 2, Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.AQUA, PERCENT);
        updateStormBar();
        earthquakeHazard = settings.earthquakeHazard;
        earthQuakeSlider = new MenuSlider(inventory, 11, Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.GOLD, PERCENT);
        updateEarthquakeBar();
        ghostHazard = settings.ghostHazard;
        ghostSlider = new MenuSlider(inventory, 20, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, PERCENT);
        updateGhostBar();
        setPageButtons(3, true, false, null);
    }

    void updateStormBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(0, getStormItem());
        inventory.setItem(1, VOID);
        stormSlider.setProgress(stormHazard.getHazardChance().toString());
        inventory.setItem(8, VOID);
    }

    void updateEarthquakeBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(9, getEarthquakeItem());
        inventory.setItem(10, VOID);
        earthQuakeSlider.setProgress(earthquakeHazard.getHazardChance().toString());
        inventory.setItem(17, VOID);
    }

    void updateGhostBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(18, getGhostItem());
        inventory.setItem(19, VOID);
        ghostSlider.setProgress(ghostHazard.getHazardChance().toString());
        inventory.setItem(26, VOID);
    }
}