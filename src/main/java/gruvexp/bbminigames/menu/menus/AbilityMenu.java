package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.AbilityMenuRow;
import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.PlayerMenuRow;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

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

    private static final ItemStack MOD_TOGGLE = makeItem(Material.MACE, Component.text("Mod Toggle"),
    "When enabled, you can toggle", "which abilities will be allowed");
    public static final ItemStack MOD_TOGGLE_DISABLED = makeItem(77008, Component.empty());
    public static final ItemStack MOD_TOGGLE_ENABLED = makeItem(77007, Component.empty());
    public static final ItemStack ABILITY_DISABLED = makeItem(77004, Component.empty());
    public static final ItemStack ABILITY_EQUIPPED = makeItem(77005, Component.empty());

    private static final ItemStack RANDOMIZE_ABILITIES = makeItem(Material.TARGET, Component.text("Randomize abilities", NamedTextColor.LIGHT_PURPLE),
            "Click this to randomize your abilities", "from the allowed abilities");

    private static final ItemStack INDIVIDUAL_PLAYER_ABILITIES = makeItem(77010, Component.text("Edit player abilities", NamedTextColor.LIGHT_PURPLE),
            "Edit the allowed abilities", "for each individual player");

    private MenuSlider maxAbilitiesSlider;
    private MenuSlider cooldownMultiplierSlider;

    private PlayerMenuRow maxAbilitiesRow;
    private PlayerMenuRow cooldownMultiplierRow;
    private AbilityMenuRow abilityRow;

    @Override
    public Component getMenuName() {
        return Component.text("Abilities");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public boolean handlesEmptySlots() {
        return true;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) {
            clickedItem = new ItemStack(Material.AIR);
        }
        switch (clickedItem.getType()) {
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
                    s = s.substring(0, s.length() - 1);
                    settings.setAbilityCooldownMultiplier(Float.parseFloat(s));
                }
            }
            case PLAYER_HEAD -> {
                Player p = Bukkit.getPlayer(UUID.fromString(Objects.requireNonNull(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING))));
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
                if (e.getSlot() < 9) {
                    int maxAbilities = bp.getMaxAbilities(); // oppdaterer max abilities
                    maxAbilities++;
                    if (maxAbilities > 3) maxAbilities = 1;
                    bp.setMaxAbilities(maxAbilities);
                } else if (e.getSlot() <=27) {
                    float cooldownMultiplier = bp.getAbilityCooldownMultiplier(); // oppdaterer cooldownmultiplier
                    String prev = String.format(Locale.US, "%.2fx", cooldownMultiplier);
                    String next = cooldownMultiplierSlider.getNext(prev);
                    float newCooldownMultiplier = Float.parseFloat(next.substring(0, next.length() - 1));
                    bp.setAbilityCooldownMultiplier(newCooldownMultiplier);
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.hazardMenu.open(clicker);
                } else if (e.getSlot() == 49) {
                    clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED));
                }
            }
            case MACE -> BotBows.getBotBowsPlayer(clicker).toggleAbilityToggle();
            case TARGET -> clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED));
            case AIR -> {
                BotBows.debugMessage("Trykka på luft, skal vi se om det var en abilityi cursoren");
                ItemStack cursorItem = e.getCursor();
                if (cursorItem.getType() == Material.AIR) {
                    BotBows.debugMessage("Ingenting i cursoren");
                    return;
                }
                BotBows.debugMessage("Item i cursoren: " + cursorItem.getType().name());
                AbilityType type = AbilityType.fromItem(cursorItem);
                if (type == null) {
                    BotBows.debugMessage("ingen ability item i cursoren");
                    return;
                }
                BotBows.debugMessage("Ability i cursoren: " + type.name());
                BotBows.debugMessage("Plasserer i slottet: " + e.getSlot());
                if (e.getClickedInventory().equals(inventory)) {
                    BotBows.debugMessage("prøvde å plassere tilbakei menuet");
                    clicker.setItemOnCursor(null);
                    return;
                }
                if (e.getSlot() > 8) {
                    BotBows.debugMessage("prøvde å sette ned på feil sted i inventoriet");
                    return;
                }
                e.setCancelled(false);
                BotBows.debugMessage("abilitien blir nå plassert i slot " + e.getSlot());
            }
            default -> {
                AbilityType abilityType = AbilityType.fromItem(e.getCurrentItem());
                if (abilityType == null) {
                    BotBows.debugMessage("R: No ability connected to item clicked on");
                    return;
                }
                BotBowsPlayer p = BotBows.getBotBowsPlayer(clicker);
                if (p.canToggleAbilities()) {
                    settings.toggleAbility(abilityType);
                } else { // playeren plukker opp itemet (uten at det forsvinner fra menuet) og kan plassere det hvor som helst i inventoriet sitt
                    if (settings.abilityAllowed(abilityType)) {
                        BotBows.debugMessage(abilityType.name() + " is allowed");
                        if (p.isAbilityEquipped(abilityType)) {
                            p.unequipAbility(abilityType);
                            if (e.getSlot() < 9) {
                                e.setCurrentItem(null);
                            }
                        } else {
                            //e.setCancelled(false);
                            if (e.getSlot() > 36 && e.getSlot() < 45) {
                                //inventory.setItem(e.getSlot(), e.getCurrentItem());
                                p.player.setItemOnCursor(clickedItem.clone());
                                BotBows.debugMessage("Itemet ble plukka opp fra menuet");
                            } else {
                                BotBows.debugMessage("Itemet ble plukkaopp, fra inv? slot = " + e.getSlot());
                            }
                            p.equipAbility(abilityType);
                        }
                    } else {
                        clicker.sendMessage(Component.text("This ability is disabled", NamedTextColor.RED));
                    }
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
        maxAbilitiesRow = new PlayerMenuRow(inventory, 2, 5);
        cooldownMultiplierRow = new PlayerMenuRow(inventory, 20, 7);
        abilityRow = new AbilityMenuRow(inventory, 37, 8);
        setFillerVoid();
    }
    
    @Override
    public void open(Player p) {
        super.open(p);
        Inventory inv = p.getInventory();
        for (int i = 9; i < 18; i++) {
            if (inv.getItem(i) != null) {
                Main.WORLD.dropItem(p.getLocation().add(0, 5, 0), inv.getItem(i));
                inv.setItem(i, null);
            }
        }
        BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
        bp.disableAbilityToggle();
    }

    public void enableAbilities() {
        inventory.setItem(8, ABILITIES_ENABLED);
        if (individualMaxAbilities) enableIndividualMaxAbilities(); else disableIndividualMaxAbilities();
        if (individualCooldownMultipliers) enableIndividualCooldownMultiplier(); else disableIndividualCooldownMultiplier();
        abilityRow.show();
        inventory.setItem(36, MOD_TOGGLE);
        inventory.setItem(45, RANDOMIZE_ABILITIES);
        inventory.setItem(49, INDIVIDUAL_PLAYER_ABILITIES);
    }

    public void disableAbilities() {
        abilityRow.hide();
        inventory.setItem(8, ABILITIES_DISABLED);
        // fyller med gråe glassvinduer der settings var
        ItemStack disabled = makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        disableIndividualMaxAbilities();
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
        maxAbilitiesRow.show();
    }

    public void disableIndividualMaxAbilities() {
        inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_DISABLED);
        individualMaxAbilities = false;
        settings.setMaxAbilities(2);
        maxAbilitiesRow.hide();
        inventory.setItem(5, VOID);
        inventory.setItem(6, VOID);
    }

    public void enableIndividualCooldownMultiplier() {
        inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED);
        individualCooldownMultipliers = true;
        cooldownMultiplierRow.show();
    }

    public void disableIndividualCooldownMultiplier() {
        inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED);
        individualCooldownMultipliers = false;
        settings.setAbilityCooldownMultiplier(1.0f);
        cooldownMultiplierRow.hide();
    }

    public void updateMaxAbilities() {
        if (!individualMaxAbilities) {
            maxAbilitiesSlider.setProgressSlots(settings.getMaxAbilities()); // oppdaterer slideren
        }
    }

    public void updateMaxAbilities(BotBowsPlayer p) {
        ItemStack headItem = maxAbilitiesRow.getItem(p);
        headItem.setAmount(Math.max(settings.getMaxAbilities(), 1)); // oppdaterer head count
    }

    public void updateCooldownMultiplier() {
        if (!individualCooldownMultipliers) { // oppdaterer slideren
            cooldownMultiplierSlider.setProgress(String.format(Locale.US, "%.2fx", settings.getAbilityCooldownMultiplier()));
        }
    }

    public void updateCooldownMultiplier(BotBowsPlayer p) {
        ItemStack headItem = cooldownMultiplierRow.getItem(p);
        ItemMeta meta = headItem.getItemMeta();
        meta.lore(List.of(Component.text("Cooldown multiplier: ").append(Component.text(String.format(Locale.US, "%.2fx", p.getAbilityCooldownMultiplier()), NamedTextColor.LIGHT_PURPLE))));
        headItem.setItemMeta(meta);
        cooldownMultiplierRow.updatePage();
    }

    public void updateAbilityStatus(AbilityType type) {
        int index = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot();
        if (settings.abilityAllowed(type)) {
            inventory.setItem(index - 9, VOID);
        } else {
            inventory.setItem(index - 9, ABILITY_DISABLED);
        }
    }

    public int getRelativeAbilitySlot(AbilityType type) { // åssen rad det er, 0-9
        return abilityRow.getAbilitySlot(type) + 1;
    }

    public void addPlayer(BotBowsPlayer p) {
        //max abilities
        ItemStack abilitiesHead = makeHeadItem(p.player, p.getTeam().COLOR);
        abilitiesHead.setAmount(Math.max(p.getMaxAbilities(), 1));
        maxAbilitiesRow.addItem(abilitiesHead);
        // cooldown multiplier
        ItemStack cooldownHead = makeHeadItem(p.player, p.getTeam().COLOR);
        ItemMeta meta = cooldownHead.getItemMeta();
        meta.lore(List.of(Component.text("Cooldown multiplier: ").append(Component.text(String.format(Locale.US, "%.2fx", p.getAbilityCooldownMultiplier()), NamedTextColor.LIGHT_PURPLE))));
        cooldownHead.setItemMeta(meta);
        cooldownMultiplierRow.addItem(cooldownHead);
    }

    public void removePlayer(BotBowsPlayer p) {
        removePlayerFromRow(p, maxAbilitiesRow);
        removePlayerFromRow(p, cooldownMultiplierRow);
    }

    private void removePlayerFromRow(BotBowsPlayer p, PlayerMenuRow row) {
        row.removeItem(row.getItem(p));
    }
}
