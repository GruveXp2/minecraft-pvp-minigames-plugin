package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.*;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.settings.AbilitySettings;
import gruvexp.bbminigames.twtClassic.settings.AbilityUpdateListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

import java.util.*;

public class AbilityMenu extends SettingsMenu implements AbilityUpdateListener {

    private static final ItemStack ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow"));

    private static final ItemStack ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow"));

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap"));

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap"));

    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier"));

    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier"));

    private static final ItemStack UNIQUE_MODE_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Unique mode", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each ability"), Component.text("can can only be equipped by max team member"));

    private static final ItemStack UNIQUE_MODE_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Unique mode", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can can only be equipped by max team member"));

    private static final ItemStack MOD_TOGGLE = makeItem(Material.MACE, Component.text("Mod Toggle"),
    Component.text("When enabled, you can toggle"), Component.text("which abilities will be allowed"));

    public static final ItemStack MOD_TOGGLE_DISABLED = makeItem("inactive_slot_covered", Component.empty());
    public static final ItemStack MOD_TOGGLE_ENABLED = makeItem("active_slot_covered", Component.empty());
    public static final ItemStack ABILITY_DISABLED = makeItem("disabled_slot_covered", Component.empty());
    public static final ItemStack ABILITY_TAKEN = makeItem("yellow_slot_covered", Component.empty());
    public static final ItemStack ABILITY_EQUIPPED = makeItem("enabled_slot_covered", Component.empty());

    private static final ItemStack RANDOMIZE_ABILITIES = makeItem(Material.TARGET, Component.text("Randomize abilities", NamedTextColor.LIGHT_PURPLE),
            Component.text("Click this to randomize your abilities"), Component.text("from the allowed abilities"));

    private static final ItemStack INDIVIDUAL_PLAYER_ABILITIES = makeItem("gear", Component.text("Edit player abilities", NamedTextColor.LIGHT_PURPLE),
            Component.text("Edit the allowed abilities"), Component.text("for each individual player"));

    private final BotBowsPlayer bp;

    private MenuSlider maxAbilitiesSlider;
    private MenuSlider cooldownMultiplierSlider;

    private PlayerMenuRow maxAbilitiesRow;
    private PlayerMenuRow cooldownMultiplierRow;
    private AbilityMenuRow abilityRow;

    public AbilityMenu(Settings settings, BotBowsPlayer bp) {
        super(settings);
        this.bp = bp;
        updateUIState();
    }

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
        BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(clicker);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) {
            clickedItem = new ItemStack(Material.AIR);
        }
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        switch (clickedItem.getType()) {
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getClickedInventory() != inventory) return;
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> {
                        abilitySettings.setIndividualMax(false);
                        abilitySettings.setMaxAbilities(2);
                    }
                    case 8 -> abilitySettings.setMaxAbilities(0);
                    case 18 -> abilitySettings.setIndividualCooldown(false);
                    case 53 -> abilitySettings.setUniqueMode(false);
                }
            }
            case RED_STAINED_GLASS_PANE -> {
                if (e.getClickedInventory() != inventory) return;
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> abilitySettings.setIndividualMax(true);
                    case 8 -> abilitySettings.setMaxAbilities(2);
                    case 18 -> abilitySettings.setIndividualCooldown(true);
                    case 53 -> abilitySettings.setUniqueMode(true);
                }
            }
            case WHITE_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                if (e.getSlot() < 9) {
                    abilitySettings.setMaxAbilities(Integer.parseInt(s));
                } else if (e.getSlot() <=27) {
                    s = s.substring(0, s.length() - 1);
                    abilitySettings.setCooldownMultiplier(Float.parseFloat(s));
                }
            }
            case PLAYER_HEAD -> {
                if (PlainTextComponentSerializer.plainText().serialize(clickedItem.displayName()).contains("Laser")) {
                    handleAbilityClick(e, clicker, bp, clickedItem);
                    return;
                }
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                NamespacedKey key = new NamespacedKey(Main.getPlugin(), "uuid");
                UUID playerId = UUID.fromString(Objects.requireNonNull(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                BotBowsPlayer headBp = settings.lobby.getBotBowsPlayer(playerId);
                if (e.getSlot() < 9) {
                    int maxAbilities = headBp.getMaxAbilities(); // oppdaterer max abilities
                    maxAbilities++;
                    if (maxAbilities > 3) maxAbilities = 1;
                    headBp.setMaxAbilities(maxAbilities);
                } else if (e.getSlot() <=27) {
                    float cooldownMultiplier = headBp.getAbilityCooldownMultiplier(); // oppdaterer cooldownmultiplier
                    String prev = String.format(Locale.US, "%.2fx", cooldownMultiplier);
                    String next = cooldownMultiplierSlider.getNext(prev);
                    float newCooldownMultiplier = Float.parseFloat(next.substring(0, next.length() - 1));
                    headBp.setAbilityCooldownMultiplier(newCooldownMultiplier);
                }
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.hazardMenu.open(clicker);
                } else if (e.getSlot() == 49) {
                    clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED));
                } else if (e.getSlot() == 37) {
                    abilityRow.prevPage();
                } else if (e.getSlot() == 44) {
                    abilityRow.nextPage();
                }
            }
            case MACE -> {
                BotBowsPlayer p = settings.lobby.getBotBowsPlayer(clicker);
                if (!settings.playerIsMod(p)) return;

                p.toggleAbilityToggle();
            }
            case TARGET -> {
                new HashSet<>(bp.getAbilities()).forEach(ability -> bp.unequipAbility(ability.getType(), true));

                List<AbilityType> abilityTypes = new ArrayList<>(List.of(AbilityType.values()));
                Collections.shuffle(abilityTypes);
                for (AbilityType abilityType : abilityTypes) {
                    if (bp.getTotalAbilities() == bp.getMaxAbilities()) break;
                    if (!abilitySettings.isBanned(abilityType)) bp.equipAbility(abilityType);
                }
            }
            case ARROW -> {
                if (e.getClickedInventory() != inventory) e.setCancelled(true);
            }
            default -> handleAbilityClick(e, clicker, bp, clickedItem);
        }
    }

    private void handleAbilityClick(InventoryClickEvent e, Player p, BotBowsPlayer bp, ItemStack clickedItem) {
        ItemStack cursorItem = e.getCursor();
        AbilityType cursorAbility = AbilityType.fromItem(cursorItem);
        AbilityType clickedAbility = AbilityType.fromItem(clickedItem);

        if (cursorAbility == null && clickedAbility == null) return;

        Inventory menuInventory = inventory;
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        if (e.getClickedInventory() == menuInventory) {
            if (cursorAbility != null) {
                p.setItemOnCursor(null);
                if (bp.hasAbilityEquipped(cursorAbility)) {
                    bp.unequipAbility(cursorAbility);
                }
            } else {
                if (bp.isToggleAbilityMode()) {
                    abilitySettings.toggle(clickedAbility);
                    return;
                }
                if (cursorItem.getType() != Material.AIR) return;

                if (abilitySettings.isBanned(clickedAbility)) {
                    p.sendMessage(Component.text("This ability is disabled", NamedTextColor.RED));
                    return;
                }

                if (bp.hasAbilityEquipped(clickedAbility)) {
                    bp.unequipAbility(clickedAbility);
                } else { // picking up ability from menu
                    boolean clickedOnMenuAbilityRow = e.getSlot() > 36 && e.getSlot() < 45;
                    if (clickedOnMenuAbilityRow) {
                        if (bp.getTotalAbilities() == bp.getMaxAbilities()) {
                            if (bp.getMaxAbilities() == 0) {
                                p.sendMessage(Component.text("The mod has disabled abilities for you", NamedTextColor.RED));
                            } else {
                                p.sendMessage(Component.text("Ability limit reached", NamedTextColor.RED));
                            }
                            return;
                        } else if (abilitySettings.isUniqueMode() && abilitySettings.isEquippedByTeam(bp, clickedAbility)) {
                            p.sendMessage(Component.text("This ability is already equipped by other team members (unique abilities is on)", NamedTextColor.YELLOW));
                            return;
                        }

                        ItemStack abilityItem = clickedItem.clone();
                        ItemMeta meta = abilityItem.getItemMeta();

                        Component cooldownComponent = clickedAbility.getCooldownComponent(bp);
                        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
                        lore.set(lore.size() - 1, cooldownComponent);
                        meta.lore(lore);

                        abilityItem.setItemMeta(meta);
                        p.setItemOnCursor(abilityItem);
                    }
                }
            }
        } else { // clicked in player inventory
            if (cursorItem.getType() == Material.AIR) {
                if (bp.hasAbilityEquipped(clickedAbility)) { // picks up ability to move it around
                    bp.equipAbility(-1, clickedAbility);
                }
            } else { // places ability down in that slot
                if (bp.getTotalAbilities() == bp.getMaxAbilities() && !bp.hasAbilityEquipped(cursorAbility)) return;

                if (e.getSlot() >= 9) { // clicks somewhere else than hotbar
                    p.setItemOnCursor(null);
                    if (bp.hasAbilityEquipped(cursorAbility)) {
                        bp.unequipAbility(cursorAbility);
                    }
                    return;
                }
                bp.equipAbility(e.getSlot(), cursorAbility, false);

                if (clickedAbility != null) {
                    bp.equipAbility(-1, clickedAbility);
                }
            }
        }
    }

    public void handleMenuClose(InventoryCloseEvent e) {
        handleMenuClose((Player) e.getPlayer());
    }

    public void handleMenuClose(Player p) {
        Inventory inv = p.getInventory();
        for (int i = 9; i < 18; i++) { // fjerner menu overlay greier
            if (inv.getItem(i) != null && inv.getItem(i).getType() == Material.FIREWORK_STAR) {
                inv.setItem(i, null);
            }
        }
    }

    @Override
    public void setMenuItems() {
        setPageButtons(5, true, false);
        maxAbilitiesSlider = new MenuSlider(inventory, 2, Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.GREEN, List.of("1", "2", "3"), "Max abilities");
        cooldownMultiplierSlider = new MenuSlider(inventory, 20, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE,
                List.of("0.25x", "0.50x", "0.75x", "1.00x", "1.25x", "1.50x", "2.00x"), "Cooldown multiplier");
        maxAbilitiesRow = new PlayerMenuRow(inventory, 2, 5);
        cooldownMultiplierRow = new PlayerMenuRow(inventory, 20, 7);
        abilityRow = new AbilityMenuRow(inventory, 37, 8, this);
        setFillerVoid();
    }

    public void updateUIState() {
        if (settings.getAbilitySettings().getMaxAbilities() > 0) {
            inventory.setItem(8, ABILITIES_ENABLED);
            updateMaxAbilitiesUIState();
            updateCooldownMultiplierUIState();
            onUniqueModeToggle();
            abilityRow.show();
            inventory.setItem(36, MOD_TOGGLE);
            inventory.setItem(45, RANDOMIZE_ABILITIES);
            inventory.setItem(49, INDIVIDUAL_PLAYER_ABILITIES);
        } else {
            abilityRow.hide();
            maxAbilitiesRow.hide();
            cooldownMultiplierRow.hide();
            inventory.setItem(8, ABILITIES_DISABLED);
            // fyller med gråe glassvinduer der settings var
            updateMaxAbilitiesUIState();
            inventory.setItem(0, DISABLED_SLOT);
            inventory.setItem(53, DISABLED_SLOT);
            inventory.setItem(18, DISABLED_SLOT);
            for (int i = 20; i < 27; i++) {
                inventory.setItem(i, DISABLED_SLOT);
            }
            for (int i = 27; i < 36; i++) {
                inventory.setItem(i, VOID);
            }
            for (int i = 36; i < 45; i++) {
                inventory.setItem(i, DISABLED_SLOT);
            }
        }
    }

    public void updateMaxAbilitiesUIState() {
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        if (abilitySettings.isIndividualMax()) {
            inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_ENABLED);
            maxAbilitiesRow.show();
        } else {
            inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_DISABLED);
            maxAbilitiesRow.hide();
            maxAbilitiesSlider.setProgressSlots(abilitySettings.getMaxAbilities());
            settings.getPlayers().forEach(p -> p.setMaxAbilities(abilitySettings.getMaxAbilities())); // temporary until individual ability settings get moved into AbilitySettings
            inventory.setItem(5, VOID);
            inventory.setItem(6, VOID);
        }
    }

    public void updateCooldownMultiplierUIState() {
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        if (abilitySettings.isIndividualCooldown()) {
            inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED);
            cooldownMultiplierRow.show();
        } else {
            inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED);
            abilitySettings.setCooldownMultiplier(1.0f);
            cooldownMultiplierRow.hide();
        }
    }

    @Override
    public void onAbilitiesToggle() {
        updateUIState();
    }

    @Override
    public void onMaxAbilitiesChange() {
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        if (abilitySettings.isIndividualMax()) {
            settings.getPlayers().forEach(this::updateMaxAbilities);
        } else {
            maxAbilitiesSlider.setProgressSlots(abilitySettings.getMaxAbilities()); // oppdaterer slideren
            settings.getPlayers().forEach(p -> p.setMaxAbilities(abilitySettings.getMaxAbilities())); // temporary until individual ability settings get moved into AbilitySettings
        }
    }

    public void updateMaxAbilities(BotBowsPlayer p) {
        if (!settings.getAbilitySettings().isIndividualMax()) return;
        ItemStack headItem = maxAbilitiesRow.getItem(p);
        headItem.setAmount(Math.max(p.getMaxAbilities(), 1)); // oppdaterer head count
        maxAbilitiesRow.displayRow();
    }

    @Override
    public void onIndividualMaxToggle() {
        updateMaxAbilitiesUIState();
    }
    @Override
    public void onCooldownMultiplierChange() {
        AbilitySettings abilitySettings = settings.getAbilitySettings();
        if (abilitySettings.isIndividualCooldown()) {
            settings.getPlayers().forEach(this::updateCooldownMultiplier);
        } else {
            cooldownMultiplierSlider.setProgress(String.format(Locale.US, "%.2fx", abilitySettings.getCooldownMultiplier()));
        }
    }

    public void updateCooldownMultiplier(BotBowsPlayer p) {
        if (!settings.getAbilitySettings().isIndividualCooldown()) return;
        ItemStack headItem = cooldownMultiplierRow.getItem(p);
        ItemMeta meta = headItem.getItemMeta();
        meta.lore(List.of(Component.text("Cooldown multiplier: ")
                .append(Component.text(String.format(Locale.US, "%.2fx", p.getAbilityCooldownMultiplier()), NamedTextColor.LIGHT_PURPLE))));
        headItem.setItemMeta(meta);
        cooldownMultiplierRow.displayRow();
    }

    @Override
    public void onIndividualCooldownToggle() {
        updateCooldownMultiplierUIState();
    }

    @Override
    public void onUniqueModeToggle() {
        if (settings.getAbilitySettings().isUniqueMode()) {
            inventory.setItem(53, UNIQUE_MODE_ENABLED);

            // unequips abilities from team members if a teammate already has it equipped
            var toUnequip = bp.getAbilities().stream()
                    .map(Ability::getType)
                    .filter(type -> !settings.getAbilitySettings().attemptEquip(bp, type))
                    .toList();

            toUnequip.forEach(bp::unequipAbility);
        } else {
            inventory.setItem(53, UNIQUE_MODE_DISABLED);
            updateAbilityStatuses();
        }
    }

    @Override
    public void onAbilityStatusChange(@NotNull AbilityType type) {
        int slot = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot();
        if (settings.getAbilitySettings().isBanned(type)) {
            inventory.setItem(slot - 9, ABILITY_DISABLED);
        } else {
            inventory.setItem(slot - 9, VOID);
        }
    }

    public void updateAbilityStatuses() {
        for (int i = 0; i < abilityRow.size; i++) {
            int abilitySlot = abilityRow.startSlot + i;
            ItemStack abilityItem = inventory.getItem(abilitySlot);
            AbilityType abilityType = AbilityType.fromItem(abilityItem);
            if (abilityType == null) {
                inventory.setItem(abilitySlot - 9, VOID);
            } else if (bp.hasAbilityEquipped(abilityType)) {
                inventory.setItem(abilitySlot - 9, ABILITY_EQUIPPED);
            } else if (settings.getAbilitySettings().isBanned(abilityType)) {
                inventory.setItem(abilitySlot - 9, ABILITY_DISABLED);
            } else if (settings.getAbilitySettings().isUniqueMode() && settings.getAbilitySettings().isEquippedByTeam(bp, abilityType)) {
                inventory.setItem(abilitySlot - 9, ABILITY_TAKEN);
            } else {
                inventory.setItem(abilitySlot - 9, VOID);
            }
        }
    }

    public int getRelativeAbilitySlot(AbilityType type) { // åssen rad det er, 0-9. negative verdier hvis det er på feil side
        int slot = abilityRow.getAbilitySlot(type) + 1;
        if (slot > abilityRow.size) return -1;
        return slot;
    }

    public void addPlayer(BotBowsPlayer p) {
        //max abilities
        ItemStack abilitiesHead = p.avatar.getHeadItem();
        abilitiesHead.setAmount(Math.max(p.getMaxAbilities(), 1));
        maxAbilitiesRow.addItem(abilitiesHead);
        // cooldown multiplier
        ItemStack cooldownHead = p.avatar.getHeadItem();
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

    @Override
    public void onUniqueAbilityOccupancyChange(@NotNull AbilityType type, @NotNull BotBowsPlayer bp, boolean equipped) {
        if (bp == this.bp || bp.getTeam() != this.bp.getTeam()) return;
        int slot = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot();
        if (equipped) {
            inventory.setItem(slot - 9, ABILITY_TAKEN);
        } else {
            inventory.setItem(slot - 9, VOID);
        }
    }
}
