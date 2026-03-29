package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import gruvexp.bbminigames.twtClassic.settings.HazardSettings;
import gruvexp.bbminigames.twtClassic.settings.HazardUpdateListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HazardMenu extends SettingsMenu implements HazardUpdateListener {

    private static final List<String> PERCENT = HazardChance.getPercentStrings();
    private final ArrayList<HazardType> hazardsSorted = new ArrayList<>();

    private final HashMap<HazardType, MenuSlider> hazardSliders = new HashMap<>();

    public HazardMenu(Settings settings) {
        super(settings);
    }

    private ItemStack getHazardItem(Hazard hazard) {
        if (hazard == null) {
            return makeItem(Material.BARRIER, Component.text("Unavailable", NamedTextColor.RED),
                    Component.text("This hazard is not compatible with this map", NamedTextColor.RED));
        }
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
        if (e.getClickedInventory() != inventory) return;
        BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(clicker);
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(bp)) return;

        HazardSettings hazardSettings = settings.getHazardSettings();
        switch (e.getCurrentItem().getType()) {
            case WHITE_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, BROWN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                HazardChance chance = HazardChance.of(s);
                int row = e.getSlot() / 9;
                HazardType type = hazardsSorted.get(row);
                hazardSettings.setChance(type, chance);
            } case RED_STAINED_GLASS_PANE -> {
                int row = e.getSlot() / 9;
                HazardType type = hazardsSorted.get(row);
                hazardSettings.resetChance(type);
            } case LIME_STAINED_GLASS_PANE -> {
                int row = e.getSlot() / 9;
                HazardType type = hazardsSorted.get(row);
                hazardSettings.setChance(type, HazardChance.DISABLED);
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.winConditionMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.abilityMenus.get(bp).open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        setPageButtons(3, true, true);
        setFillerVoid();
    }

    void updateBar(HazardType hazardType, int row) {
        HazardSettings hazardSettings = settings.getHazardSettings();
        inventory.setItem(row * 9, getHazardItem(hazardSettings.getHazard(hazardType)));
        hazardSliders.get(hazardType).setProgress(hazardSettings.getChance(hazardType).toString());
    }

    @Override
    public void onSchemaUpdate() {
        Set<HazardType> newHazards = settings.getHazardSettings().getHazardTypes();
        List<HazardType> updated = Arrays.stream(HazardType.values())
                .filter(newHazards::contains)
                .toList();

        hazardsSorted.stream()
                .filter(ht -> !updated.contains(ht))
                .forEach(hazardSliders::remove);

        hazardsSorted.clear();
        hazardsSorted.addAll(updated);

        // Add sliders for newly compatible hazards and recalculate the order they show in
        for (HazardType hazardType : hazardsSorted) {
            MenuSlider slider = hazardSliders.computeIfAbsent(hazardType, h ->
                    new MenuSlider(
                            inventory,
                            2 + hazardsSorted.indexOf(h) * 9,
                            h.menuFillItem,
                            h.textColor,
                            PERCENT,
                            h.name + " chance"
                    )
            );
            slider.setStartSlot(2 + hazardsSorted.indexOf(hazardType) * 9);
            updateBar(hazardType, hazardsSorted.indexOf(hazardType));
        }

        for (int i = hazardsSorted.size() * 9; i < getSlots() - 9; i++) {
            inventory.setItem(i, VOID);
        }
    }

    @Override
    public void onHazardUpdate(@NotNull HazardType type) {
        int row = hazardsSorted.indexOf(type);
        updateBar(type, row);
    }
}