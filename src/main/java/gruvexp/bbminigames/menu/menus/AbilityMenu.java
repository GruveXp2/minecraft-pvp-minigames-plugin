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

    private static final ItemStack ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Abilities",
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have abilities in addition to the bow");
    private static final ItemStack ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Abilities",
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have abilities in addition to the bow");

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Individual max abilities",
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have a different max ability cap");
    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Individual max abilities",
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

    public void disableAbilities() {
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
        updateMaxAbilities();
    }

    public void disableIndividualMaxAbilities() {
        inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_DISABLED);
        individualMaxAbilities = false;
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
