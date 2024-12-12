package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.PaginatedMenuRow;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AbilityMenu extends SettingsMenu {

    private boolean individualMaxAbilities = false;
    private boolean individualCooldownMultipliers = false;

    private static final ItemStack ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.RED),
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have abilities in addition to the bow");
    private static final ItemStack ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.GREEN),
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have abilities in addition to the bow");

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.RED),
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have a different max ability cap");
    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.GREEN),
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have a different max ability cap");

    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.RED),
            ChatColor.DARK_RED + "Disabled", "By enabling this, each player", "can have a different cooldown multiplier");
    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.GREEN),
            ChatColor.DARK_GREEN + "Enabled", "By enabling this, each player", "can have a different cooldown multiplier");

    private MenuSlider maxAbilitiesSlider;
    private MenuSlider cooldownMultiplierSlider;

    private PaginatedMenuRow maxAbilitiesRow;
    private PaginatedMenuRow cooldownMultiplierRow;

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
                    case 18 -> disableIndividualCooldownMultiplier();
                }
            }
            case RED_STAINED_GLASS_PANE -> {
                switch (e.getSlot()) {
                    case 0 -> enableIndividualMaxAbilities();
                    case 8 -> enableAbilities();
                    case 18 -> enableIndividualCooldownMultiplier();
                }
            }
            case WHITE_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                if (e.getSlot() < 9) {
                    settings.setMaxAbilities(Integer.parseInt(s));
                } else if (e.getSlot() <=27) {
                    settings.setAbilityCooldownMultiplier(Float.parseFloat(s));
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.hazardMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems(); // initer settings
        setPageButtons(5, true, false, null);
        maxAbilitiesSlider = new MenuSlider(inventory, 2, Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.GREEN, List.of("1", "2", "3"));
        cooldownMultiplierSlider = new MenuSlider(inventory, 20, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, List.of("0.25x", "0.50x", "0.75x", "1.00x", "1.25x", "1.50x", "2.00x"));
        maxAbilitiesRow = new PaginatedMenuRow(inventory, 2, 5);
        cooldownMultiplierRow = new PaginatedMenuRow(inventory, 20, 7);
        setFillerVoid();
    }

    public void enableAbilities() {
        inventory.setItem(8, ABILITIES_ENABLED);
        if (individualMaxAbilities) enableIndividualMaxAbilities(); else disableIndividualMaxAbilities();
        if (individualCooldownMultipliers) enableIndividualCooldownMultiplier(); else disableIndividualCooldownMultiplier();
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
        settings.setMaxAbilities(2);
        inventory.setItem(5, VOID);
        inventory.setItem(6, VOID);
    }

    public void enableIndividualCooldownMultiplier() {
        inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED);
        individualCooldownMultipliers = true;
        updateCooldownMultipliers();
    }

    public void disableIndividualCooldownMultiplier() {
        inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED);
        individualCooldownMultipliers = false;
        settings.setAbilityCooldownMultiplier(1.0f);
    }

    public void updateMaxAbilities() {
        if (individualMaxAbilities) {
            int sliderStartSlot = maxAbilitiesSlider.getStartSlot();
            if (settings.getPlayers().size() > 5) {
                inventory.setItem(sliderStartSlot, makeItem(77011, "Set max abilities", "Click to expand"));
                for (int i = 1; i < 5; i++) {
                    inventory.setItem(i + sliderStartSlot, VOID);
                }
            } else {
                for (int i = 0; i < 5; i++) { // setter av plass til playerheads
                    inventory.setItem(i + sliderStartSlot, null);
                }
                placeHeads(settings.team1, sliderStartSlot);
                placeHeads(settings.team2, sliderStartSlot + settings.team2.size());
            }

        } else { // en slider
            maxAbilitiesSlider.setProgressSlots(settings.getMaxAbilities());
        }
    }

    public void updateCooldownMultipliers() {
        if (individualCooldownMultipliers) {
            int sliderSize = cooldownMultiplierSlider.size();
            int sliderStartSlot = cooldownMultiplierSlider.getStartSlot();
            if (settings.getPlayers().size() > sliderSize) {
                inventory.setItem(sliderStartSlot, makeItem(77011, "Set cooldown multipliers", "Click to expand"));
                for (int i = 1; i < sliderSize; i++) {
                    inventory.setItem(i + sliderStartSlot, VOID);
                }
            } else {
                for (int i = 0; i < sliderSize; i++) { // setter av plass til playerheads
                    inventory.setItem(i + sliderStartSlot, null);
                }
                placeHeads(settings.team1, sliderStartSlot);
                placeHeads(settings.team2, sliderStartSlot + settings.team2.size());
            }
        } else {
            cooldownMultiplierSlider.setProgress(String.format("%.2fx", settings.getAbilityCooldownMultiplier()));
        }
    }

    private void placeHeads(BotBowsTeam team, int startSlot) {
        for (int i = 0; i < team.size(); i++) {
            BotBowsPlayer p = team.getPlayer(i);
            ItemStack headItem = makeHeadItem(p.PLAYER, p.getTeam().COLOR);
            headItem.setAmount(p.getMaxAbilities());
            inventory.setItem(startSlot + i, headItem);
        }
    }
}
