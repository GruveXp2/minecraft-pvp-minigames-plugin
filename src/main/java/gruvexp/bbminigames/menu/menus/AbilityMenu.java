package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

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
    public static final ItemStack MOD_TOGGLE_DISABLED = makeItem("inactive_slot", Component.empty());
    public static final ItemStack MOD_TOGGLE_ENABLED = makeItem("active_slot", Component.empty());
    public static final ItemStack ABILITY_DISABLED = makeItem("disabled_slot_covered", Component.empty());
    public static final ItemStack ABILITY_EQUIPPED = makeItem("enabled_slot", Component.empty());

    private static final ItemStack RANDOMIZE_ABILITIES = makeItem(Material.TARGET, Component.text("Randomize abilities", NamedTextColor.LIGHT_PURPLE),
            "Click this to randomize your abilities", "from the allowed abilities");

    private static final ItemStack INDIVIDUAL_PLAYER_ABILITIES = makeItem("gear", Component.text("Edit player abilities", NamedTextColor.LIGHT_PURPLE),
            "Edit the allowed abilities", "for each individual player");

    private MenuSlider maxAbilitiesSlider;
    private MenuSlider cooldownMultiplierSlider;

    private PlayerMenuRow maxAbilitiesRow;
    private PlayerMenuRow cooldownMultiplierRow;
    private AbilityMenuRow abilityRow;

    @Override
    public Component getMenuName() {
        return Component.text("Abilities (6/6)");
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
                if (!settings.playerIsMod(BotBows.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> disableIndividualMaxAbilities();
                    case 8 -> disableAbilities();
                    case 18 -> disableIndividualCooldownMultiplier();
                }
            }
            case RED_STAINED_GLASS_PANE -> {
                if (!settings.playerIsMod(BotBows.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> enableIndividualMaxAbilities();
                    case 8 -> enableAbilities();
                    case 18 -> enableIndividualCooldownMultiplier();
                }
            }
            case WHITE_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                if (!settings.playerIsMod(BotBows.getBotBowsPlayer(clicker))) return;

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
                if (!settings.playerIsMod(BotBows.getBotBowsPlayer(clicker))) return;

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
            case MACE -> {
                BotBowsPlayer p = BotBows.getBotBowsPlayer(clicker);
                if (!settings.playerIsMod(p)) return;

                p.toggleAbilityToggle();
            }
            case TARGET -> clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED));
            case AIR -> {
                ItemStack cursorItem = e.getCursor();
                if (cursorItem.getType() == Material.AIR) return;
                AbilityType type = AbilityType.fromItem(cursorItem);
                if (type == null) {
                    BotBows.debugMessage("ingen ability item i cursoren", TestCommand.test2);
                    return;
                }
                BotBows.debugMessage("Ability i cursoren: " + type.name());
                BotBows.debugMessage("Plasserer i slottet: " + e.getSlot());
                if (e.getClickedInventory().equals(inventory)) {
                    BotBows.debugMessage("prøvde å plassere tilbakei menuet", TestCommand.test2);
                    clicker.setItemOnCursor(null);
                    return;
                }
                if (e.getSlot() > 8 || e.getSlot() == 0) {
                    BotBows.debugMessage("prøvde å sette ned på feil sted i inventoriet", TestCommand.test2);
                    return;
                }
                BotBowsPlayer p = BotBows.getBotBowsPlayer(clicker);
                if (p.getTotalAbilities() == p.getMaxAbilities()) {
                    clicker.sendMessage(Component.text("Max ability count reached", NamedTextColor.RED));
                    return;
                }
                e.setCancelled(false);
                BotBows.debugMessage("abilitien blir nå plassert i slot " + e.getSlot(), TestCommand.test2);
                p.equipAbility(e.getSlot(), type);
            }
            default -> {
                AbilityType abilityType = AbilityType.fromItem(e.getCurrentItem());
                if (abilityType == null) {
                    //BotBows.debugMessage("R: No ability connected to item clicked on");
                    return;
                }
                BotBowsPlayer p = BotBows.getBotBowsPlayer(clicker);
                if (p.canToggleAbilities()) {
                    settings.toggleAbility(abilityType);
                } else { // playeren plukker opp itemet (uten at det forsvinner fra menuet) og kan plassere det hvor som helst i inventoriet sitt
                    if (settings.abilityAllowed(abilityType)) {
                        if (p.isAbilityEquipped(abilityType)) {
                            p.unequipAbility(abilityType);
                        } else {
                            if (e.getSlot() > 36 && e.getSlot() < 45) {
                                ItemStack cursorItem = clickedItem.clone();
                                ItemMeta meta = cursorItem.getItemMeta();
                                Component cooldownComponent = getCooldownComponent(p, abilityType);
                                meta.lore(List.of(cooldownComponent));
                                cursorItem.setItemMeta(meta);
                                p.player.setItemOnCursor(cursorItem);
                            }
                        }
                    } else {
                        clicker.sendMessage(Component.text("This ability is disabled", NamedTextColor.RED));
                    }
                }
            }
        }
    }

    private static @NotNull Component getCooldownComponent(BotBowsPlayer p, AbilityType abilityType) {
        int percentage = (int) ((p.getAbilityCooldownMultiplier() - 1) * 100);
        Component cooldownComponent = Component.text("Cooldown: ").append(Component.text((int) (abilityType.getBaseCooldown() * p.getAbilityCooldownMultiplier()) + "s", NamedTextColor.YELLOW));
        if (percentage != 0) {
            cooldownComponent = cooldownComponent.append(Component.text(" (" + (percentage > 0 ? "+" : "") + percentage + "%)", percentage < 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        }
        return cooldownComponent;
    }

    public void handleMenuClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inv = p.getInventory();
        for (int i = 9; i < 18; i++) { // fjerner menu overlay greier
            if (inv.getItem(i) != null && inv.getItem(i).getType() == Material.FIREWORK_STAR) {
                inv.setItem(i, null);
            }
        }
    }

    @Override
    public void setMenuItems() {
        super.setMenuItems(); // initer settings
        setPageButtons(5, true, false);
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
        bp.getAbilities().forEach(ability -> inv.setItem(9 + getRelativeAbilitySlot(ability.getType()), ABILITY_EQUIPPED));
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
        headItem.setAmount(Math.max(p.getMaxAbilities(), 1)); // oppdaterer head count
        maxAbilitiesRow.updatePage();
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
