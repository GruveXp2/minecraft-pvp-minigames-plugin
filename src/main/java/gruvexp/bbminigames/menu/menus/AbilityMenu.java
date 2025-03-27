package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.AbilityMenuRow;
import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.PlayerMenuRow;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AbilityMenu extends SettingsMenu {

    private boolean individualMaxAbilities = false;
    private boolean individualCooldownMultipliers = false;

    private static final ItemStack ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.RED),
            Component.text("Disabled", NamedTextColor.RED),
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow"));

    private static final ItemStack ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.GREEN),
            Component.text("Enabled", NamedTextColor.GREEN),
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow"));

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.RED),
            Component.text("Disabled", NamedTextColor.RED),
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap"));

    private static final ItemStack INDIVIDUAL_MAX_ABILITIES_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.GREEN),
            Component.text("Enabled", NamedTextColor.GREEN),
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap"));

    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.RED),
            Component.text("Disabled", NamedTextColor.RED),
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier"));

    private static final ItemStack INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.GREEN),
            Component.text("Enabled", NamedTextColor.GREEN),
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier"));

    private static final ItemStack MOD_TOGGLE = makeItem(Material.MACE, Component.text("Mod Toggle"),
    Component.text("When enabled, you can toggle"), Component.text("which abilities will be allowed"));

    public static final ItemStack MOD_TOGGLE_DISABLED = makeItem("inactive_slot", Component.empty());
    public static final ItemStack MOD_TOGGLE_ENABLED = makeItem("active_slot", Component.empty());
    public static final ItemStack ABILITY_DISABLED = makeItem("disabled_slot_covered", Component.empty());
    public static final ItemStack ABILITY_EQUIPPED = makeItem("enabled_slot", Component.empty());

    private static final ItemStack RANDOMIZE_ABILITIES = makeItem(Material.TARGET, Component.text("Randomize abilities", NamedTextColor.LIGHT_PURPLE),
            Component.text("Click this to randomize your abilities"), Component.text("from the allowed abilities"));

    private static final ItemStack INDIVIDUAL_PLAYER_ABILITIES = makeItem("gear", Component.text("Edit player abilities", NamedTextColor.LIGHT_PURPLE),
            Component.text("Edit the allowed abilities"), Component.text("for each individual player"));

    private MenuSlider maxAbilitiesSlider;
    private MenuSlider cooldownMultiplierSlider;

    private PlayerMenuRow maxAbilitiesRow;
    private PlayerMenuRow cooldownMultiplierRow;
    private AbilityMenuRow abilityRow;

    public AbilityMenu(Settings settings) {
        super(settings);
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
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) {
            clickedItem = new ItemStack(Material.AIR);
        }
        switch (clickedItem.getType()) {
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getClickedInventory() != inventory) return;
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> disableIndividualMaxAbilities();
                    case 8 -> disableAbilities();
                    case 18 -> disableIndividualCooldownMultiplier();
                }
            }
            case RED_STAINED_GLASS_PANE -> {
                if (e.getClickedInventory() != inventory) return;
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                switch (e.getSlot()) {
                    case 0 -> enableIndividualMaxAbilities();
                    case 8 -> enableAbilities();
                    case 18 -> enableIndividualCooldownMultiplier();
                }
            }
            case WHITE_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE -> {
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

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
                if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

                Player p = Bukkit.getPlayer(UUID.fromString(Objects.requireNonNull(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING))));
                BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(p);
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
            case TARGET -> clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED));
            case AIR -> { // clicked WITH ability in cursor
                ItemStack cursorItem = e.getCursor();
                if (cursorItem.getType() == Material.AIR) return;
                AbilityType type = AbilityType.fromItem(cursorItem);
                if (type == null) return;
                Inventory clickedInventory = e.getClickedInventory();
                if (clickedInventory == null) return;
                if (clickedInventory.equals(inventory)) {
                    BotBows.debugMessage("prøvde å plassere tilbakei menuet", TestCommand.test2);
                    clicker.setItemOnCursor(null);
                    return;
                }
                if (e.getSlot() > 8 || e.getSlot() == 0) {
                    BotBows.debugMessage("prøvde å sette ned på feil sted i inventoriet", TestCommand.test2);
                    clicker.setItemOnCursor(null);
                    return;
                }
                BotBowsPlayer p = settings.lobby.getBotBowsPlayer(clicker);
                if (p.getTotalAbilities() == p.getMaxAbilities()) {
                    clicker.sendMessage(Component.text("Max ability count reached", NamedTextColor.RED));
                    return;
                }
                e.setCancelled(false);
                p.equipAbility(e.getSlot(), type);
            }
            default -> { // clicked ON ability in inventory
                AbilityType abilityType = AbilityType.fromItem(e.getCurrentItem());
                if (abilityType == null) return;
                BotBows.debugMessage("clicked on ability: " + abilityType.name(), TestCommand.test2);
                BotBowsPlayer p = settings.lobby.getBotBowsPlayer(clicker);
                if (p.canToggleAbilities()) {
                    settings.toggleAbility(abilityType);
                } else { // playeren plukker opp itemet (uten at det forsvinner fra menuet) og kan plassere det hvor som helst i inventoriet sitt
                    if (settings.abilityAllowed(abilityType)) {
                        if (p.hasAbilityEquipped(abilityType)) {
                            p.unequipAbility(abilityType);
                        } else {
                            if (e.getSlot() > 36 && e.getSlot() < 45) {
                                ItemStack cursorItem = clickedItem.clone();
                                ItemMeta meta = cursorItem.getItemMeta();
                                Component cooldownComponent = getCooldownComponent(p, abilityType);
                                List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
                                lore.add(cooldownComponent);
                                meta.lore(lore);
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
    
    @Override
    public void open(Player p) {
        super.open(p);
        Inventory inv = p.getInventory();
        for (int i = 9; i < 18; i++) {
            if (inv.getItem(i) != null) {
                BotBows.debugMessage("Dropping item:" + inv.getItem(i).getType().name() + ", slot=" + i);
                var item = Main.WORLD.dropItem(p.getLocation().add(0, 5, 0), inv.getItem(i));
                item.getVelocity().add(new Vector(1, 2, 1));
                inv.setItem(i, null);
            }
        }
        BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(p);
        bp.disableAbilityToggle();
        bp.getAbilities().forEach(ability ->  {
            int abilityEquipSlot = getRelativeAbilitySlot(ability.getType());
            if (abilityEquipSlot > 0) {
                inv.setItem(abilityEquipSlot + 9, ABILITY_EQUIPPED);
            }
        });
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
        maxAbilitiesRow.hide();
        cooldownMultiplierRow.hide();
        inventory.setItem(8, ABILITIES_DISABLED);
        // fyller med gråe glassvinduer der settings var
        disableIndividualMaxAbilities();
        settings.setMaxAbilities(0);
        inventory.setItem(0, DISABLED);
        inventory.setItem(18, DISABLED);
        for (int i = 20; i < 27; i++) {
            inventory.setItem(i, DISABLED);
        }
        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, DISABLED);
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
        if (individualMaxAbilities) {
            maxAbilitiesRow.updatePage();
        }
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
        int slot = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot();
        if (settings.abilityAllowed(type)) {
            inventory.setItem(slot - 9, VOID);
        } else {
            inventory.setItem(slot - 9, ABILITY_DISABLED);
        }
    }
    
    public void updateAbilityStatuses() {
        for (int i = 0; i < abilityRow.size; i++) {
            int abilitySlot = abilityRow.startSlot + i;
            ItemStack abilityItem = inventory.getItem(abilitySlot);
            AbilityType abilityType = AbilityType.fromItem(abilityItem);
            if (abilityType == null || settings.abilityAllowed(abilityType)) {
                inventory.setItem(abilitySlot - 9, VOID);
            } else {
                inventory.setItem(abilitySlot - 9, ABILITY_DISABLED);
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
        ItemStack abilitiesHead = makeHeadItem(p.player, p.getTeamColor());
        abilitiesHead.setAmount(Math.max(p.getMaxAbilities(), 1));
        maxAbilitiesRow.addItem(abilitiesHead);
        // cooldown multiplier
        ItemStack cooldownHead = makeHeadItem(p.player, p.getTeamColor());
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
