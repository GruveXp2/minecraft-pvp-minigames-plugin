package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WinConditionMenu extends SettingsMenu {
    private static final ItemStack DYNAMIC_POINTS_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Dynamic points", NamedTextColor.RED),
            Component.text("Disabled", NamedTextColor.RED),
            Component.text("If enabled, winning team gets 1"),
            Component.text("point for each remaining hp."),
            Component.text("If disbabled, winning team only gets 1 point."));

    private static final ItemStack DYNAMIC_POINTS_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Dynamic points", NamedTextColor.GREEN),
            Component.text("Enabled", NamedTextColor.GREEN),
            Component.text("If enabled, winning team gets 1"),
            Component.text("point for each remaining hp."),
            Component.text("If disbabled, winning team only gets 1 point."));

    public WinConditionMenu(Settings settings) {
        super(settings);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Win condition (4/6)");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;
        int slot = e.getSlot();
        switch (e.getCurrentItem().getType()) {
            case RED_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(DYNAMIC_POINTS_DISABLED)) {
                    enableDynamicPoints();
                } else if (slot < 9) {
                    settings.changeWinScoreThreshold(-10);
                } else {
                    settings.changeRoundDuration(-10);
                }
            }
            case PINK_STAINED_GLASS_PANE -> {
                if (slot < 9) {
                    settings.changeWinScoreThreshold(-1);
                } else {
                    settings.changeRoundDuration(-1);
                }
            }
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(DYNAMIC_POINTS_ENABLED)) {
                    disableDynamicPoints();
                } else
                if (slot < 9) {
                    settings.changeWinScoreThreshold(1);
                } else {
                    settings.changeRoundDuration(1);
                }
            }
            case GREEN_STAINED_GLASS_PANE -> {if (slot < 9) {
                    settings.changeWinScoreThreshold(10);
                } else {
                    settings.changeRoundDuration(10);
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.healthMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.hazardMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        ItemStack sub10 = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("-10"));
        ItemStack sub1= makeItem(Material.PINK_STAINED_GLASS_PANE, Component.text("-1"));
        ItemStack add1 = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("+1"));
        ItemStack add10 = makeItem(Material.GREEN_STAINED_GLASS_PANE, Component.text("+10"));

        inventory.setItem(2, sub10);
        inventory.setItem(3, sub1);
        inventory.setItem(5, add1);
        inventory.setItem(6, add10);

        inventory.setItem(11, sub10);
        inventory.setItem(12, sub1);
        inventory.setItem(14, add1);
        inventory.setItem(15, add10);

        setPageButtons(2, true, true);
        setFillerVoid();
    }

    public void updateWinScoreThreshold() {
        ItemStack is;
        if (settings.getWinScoreThreshold() > 0) {
            is = makeItem(Material.BLUE_TERRACOTTA, Component.text("Win score threshold", NamedTextColor.BLUE));
            is.setAmount(settings.getWinScoreThreshold());
        } else {
            is = makeItem(Material.YELLOW_TERRACOTTA, Component.text("Infinite rounds", NamedTextColor.YELLOW),
                    Component.text("Run /stopgame to stop the game"));
        }
        inventory.setItem(4, is);
    }

    public void updateRoundDuration() {
        ItemStack is;
        if (settings.getRoundDuration() > 0) {
            is = makeItem(Material.BLUE_TERRACOTTA, Component.text("Round duration", NamedTextColor.BLUE), Component.text("minutes"));
            is.setAmount(settings.getRoundDuration());
        } else {
            is = makeItem(Material.YELLOW_TERRACOTTA, Component.text("No timer", NamedTextColor.YELLOW),
                    Component.text("Run /stopgame to stop the game"));
        }
        inventory.setItem(13, is);
    }

    public void enableDynamicPoints() {
        inventory.setItem(8, DYNAMIC_POINTS_ENABLED);
        settings.setDynamicScoring(true);
    }

    public void disableDynamicPoints() {
        inventory.setItem(8, DYNAMIC_POINTS_DISABLED);
        settings.setDynamicScoring(false);
    }
}
