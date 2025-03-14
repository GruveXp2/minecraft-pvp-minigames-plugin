package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

    public HazardMenu(Settings settings) {
        super(settings);
    }

    private ItemStack getHazardItem(Hazard hazard) {
        ItemStack item;
        Component[] loreDesc = hazard.getDescription();
        if (hazard.getChance() == HazardChance.DISABLED) {
            item = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text(hazard.getName(), NamedTextColor.RED),
                    Component.text("Disabled", NamedTextColor.RED),
                    Component.text("If enabled, x% of rounds " + hazard.getActionDescription() + "."), loreDesc[0], loreDesc[1], loreDesc[2]);
        } else {
            Component percentage = Component.text(hazard.getChance().getPercent() + "%", NamedTextColor.LIGHT_PURPLE);
            item = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text(hazard.getName(), NamedTextColor.GREEN),
                    Component.text("Enabled", NamedTextColor.GREEN),
                    percentage.append(Component.text(" of rounds " + hazard.getActionDescription() + ".", NamedTextColor.DARK_PURPLE)), loreDesc[0], loreDesc[1], loreDesc[2]);
        }
        return item;
    }

    @Override
    public Component getMenuName() {
        return Component.text("Hazards (5/6)");
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

        switch (e.getCurrentItem().getType()) {
            case WHITE_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                HazardChance chance = HazardChance.of(s);
                if (e.getSlot() < 9) {
                    if (stormHazard.getChance() != chance) {
                        stormHazard.setChance(chance);
                        updateStormBar();
                    }
                } else if (e.getSlot() < 18) {
                    if (earthquakeHazard.getChance() != chance) {
                        earthquakeHazard.setChance(chance);
                        updateEarthquakeBar();
                    }
                } else if (e.getSlot() < 27) {
                    if (ghostHazard.getChance() != chance) {
                        ghostHazard.setChance(chance);
                        updateGhostBar();
                    }
                }
            } case RED_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> {
                        stormHazard.setChance(HazardChance.TEN);
                        updateStormBar();
                    }
                    case 9 -> {
                        earthquakeHazard.setChance(HazardChance.TEN);
                        updateEarthquakeBar();
                    }
                    case 18 -> {
                        ghostHazard.setChance(HazardChance.TEN);
                        updateGhostBar();
                    }
                }
            } case LIME_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> {
                        stormHazard.setChance(HazardChance.DISABLED);
                        updateStormBar();
                    }
                    case 9 -> {
                        earthquakeHazard.setChance(HazardChance.DISABLED);
                        updateEarthquakeBar();
                    }
                    case 18 -> {
                        ghostHazard.setChance(HazardChance.DISABLED);
                        updateGhostBar();
                    }
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.winConditionMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.abilityMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        stormSlider = new MenuSlider(inventory, 2, Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.AQUA, PERCENT, "Storm chance");
        earthQuakeSlider = new MenuSlider(inventory, 11, Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.GOLD, PERCENT, "Earthquake chance");
        ghostSlider = new MenuSlider(inventory, 20, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, PERCENT, "Ghost chance");
        setPageButtons(3, true, true);
        setFillerVoid();
    }

    public void initMenu() {
        stormHazard = settings.stormHazard;
        updateStormBar();
        earthquakeHazard = settings.earthquakeHazard;
        updateEarthquakeBar();
        ghostHazard = settings.ghostHazard;
        updateGhostBar();
    }

    void updateStormBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(0, getHazardItem(stormHazard));
        inventory.setItem(1, VOID);
        stormSlider.setProgress(stormHazard.getChance().toString());
        inventory.setItem(8, VOID);
    }

    void updateEarthquakeBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(9, getHazardItem(earthquakeHazard));
        inventory.setItem(10, VOID);
        earthQuakeSlider.setProgress(earthquakeHazard.getChance().toString());
        inventory.setItem(17, VOID);
    }

    void updateGhostBar() { // Hvordan menu skal se ut når storm mode er enabla
        inventory.setItem(18, getHazardItem(ghostHazard));
        inventory.setItem(19, VOID);
        ghostSlider.setProgress(ghostHazard.getChance().toString());
        inventory.setItem(26, VOID);
    }
}