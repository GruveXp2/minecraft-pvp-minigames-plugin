package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AbilityMenu extends SettingsMenu {

    private boolean individualMaxAbilities = false;

    private static final ItemStack ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.RED),
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have abilities in addition to the bow");
    private static final ItemStack ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.GREEN),
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have abilities in addition to the bow");

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.RED),
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have a different max ability cap");
    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.GREEN),
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have a different max ability cap");

    @Override
    public String getMenuName() {
        return "Abilities";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case LIME_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> disableIndividualMaxAbilities();
                    case 8 -> disableAbilities();
                }
            }
            case RED_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> enableIndividualMaxAbilities();
                    case 8 -> enableAbilities();
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.winThresholdMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        disableAbilities();
        setPageButtons(5, true, false, null);
    }

    public void enableAbilities() {
        inventory.setItem(8, ABILITIES_ENABLED);
        if (individualMaxAbilities) enableIndividualMaxAbilities(); else disableIndividualMaxAbilities();
    }

    public void disableAbilities() {
        inventory.setItem(8, ABILITIES_DISABLED);
        // fyller med gråe glassvinduer der settings var
        ItemStack disabled = makeItem(Material.GRAY_STAINED_GLASS_PANE, "");
        settings.setMaxAbilities(0);
        inventory.setItem(0, disabled);
        inventory.setItem(18, disabled);
            for (int i = 20; i < 27; i++) {
                inventory.setItem(i, disabled);
        }
        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, disabled);
        }
    }

    public void enableIndividualMaxAbilities() {
        inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_ENABLED);
        individualMaxAbilities = true;
        if (settings.getMaxAbilities() == 0) settings.setMaxAbilities(2);
        updateMaxAbilities();
    }

    public void disableIndividualMaxAbilities() {
        inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_DISABLED);
        individualMaxAbilities = false;
        if (settings.getMaxAbilities() == 0) settings.setMaxAbilities(2);
        updateMaxAbilities();
        inventory.setItem(5, VOID);
        inventory.setItem(6, VOID);
    }

    public void updateMaxAbilities() {
        if (individualMaxAbilities) {
            if (settings.getPlayers().size() > 5) {
                inventory.setItem(2, makeItem(77011, "Set max abilities", "Click to expand"));
                for (int i = 1; i < 5; i++) { // setter av plass til playerheads
                    inventory.setItem(i + 2, VOID);
                }
            } else {
                for (int i = 0; i < 5; i++) { // setter av plass til playerheads
                    inventory.setItem(i + 2, null);
                }
                BotBowsTeam team1 = settings.team1;
                for (int i = 0; i < team1.size(); i++) {
                    BotBowsPlayer p = team1.getPlayer(i);
                    ItemStack headItem = makeHeadItem(p.PLAYER, p.getTeam().COLOR);
                    headItem.setAmount(p.getMaxAbilities());
                    inventory.setItem(i + 2, headItem);
                }
            }

        } else { // en slider
            int maxAbilities = settings.getMaxAbilities();
            for (int i = 0; i < 3; i++) {
                ItemStack is = makeItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + String.valueOf(i + 1));
                if (i < maxAbilities) {
                    is = makeItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.RED + String.valueOf(i + 1));
                }
                inventory.setItem(i + 2, is);
            }
        }
    }
}
