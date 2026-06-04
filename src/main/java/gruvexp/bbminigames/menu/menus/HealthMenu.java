package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HealthMenu extends SettingsMenu {

    private static final ItemStack CUSTOM_HP_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp"));

    private static final ItemStack CUSTOM_HP_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp"));

    private static final ItemStack CUSTOM_DAMAGE_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Custom Damage", NamedTextColor.RED),
            SettingsMenu.STATUS_DISABLED,
            Component.text("By enabling this, each player"),
            Component.text("ca do different amounts of damage"));

    private static final ItemStack CUSTOM_DAMAGE_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Custom Damage", NamedTextColor.GREEN),
            SettingsMenu.STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can do different amounts of damage"));

    private final MenuSlider healthSlider;

    public HealthMenu(Settings settings) {
        super(settings);
        setPageButtons(2, true, true);
        healthSlider = new MenuSlider(inventory, 2, Material.PINK_STAINED_GLASS_PANE, NamedTextColor.RED, List.of("1", "2", "3", "4", "5"), "Health");
    }

    @Override
    public Component getMenuName() {
        return Component.text("Health & Damage (3/6)");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (e.getClickedInventory() != inventory) return;
        if (handlePageClick(e)) return;
        if (!settings.checkMod(settings.lobby.getBotBowsPlayer(clicker))) return;

        switch (e.getCurrentItem().getType()) {
            case WHITE_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE -> {
                Component c = e.getCurrentItem().getItemMeta().displayName();
                assert c != null;
                String s = PlainTextComponentSerializer.plainText().serialize(c);
                settings.setMaxHP(Integer.parseInt(s));
                updateMenu();
            }
            case RED_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(CUSTOM_HP_DISABLED)) {
                    settings.setCustomHPEnabled(true);
                } else if (e.getCurrentItem().equals(CUSTOM_DAMAGE_DISABLED)) {
                    settings.setCustomDamageEnabled(true);
                }
            }
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(CUSTOM_HP_ENABLED)) {
                    settings.setCustomHPEnabled(false);
                } else if (e.getCurrentItem().equals(CUSTOM_DAMAGE_ENABLED)) {
                    settings.setCustomDamageEnabled(false);
                }
            }
            case PLAYER_HEAD -> {
                ItemStack head = e.getCurrentItem();
                NamespacedKey key = new NamespacedKey(Main.getPlugin(), "uuid");
                UUID playerId = UUID.fromString(Objects.requireNonNull(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(playerId);
                int slot = e.getSlot();
                if (slot < 9) {
                    int maxHP = head.getAmount();
                    if (maxHP > 9) {
                        maxHP += 5;
                        if (maxHP > 20) {
                            maxHP = 1;
                        }
                    } else {
                        maxHP += 1;
                    }
                    bp.setMaxHP(maxHP);
                    head.setAmount(maxHP);
                } else {
                    int attackDamage = head.getAmount();
                    attackDamage += 1;
                    if (attackDamage > 5) {
                        attackDamage = 1;
                    }
                    bp.setAttackDamage(attackDamage);
                    head.setAmount(attackDamage);
                }
                inventory.setItem(slot, head);
            }
        }
    }

    @Override
    public void prevPage(Player p) {
        settings.teamsMenu.open(p);
    }

    @Override
    public void nextPage(Player p) {
        settings.winConditionMenu.open(p);
    }

    public void updateMenu() {
        updateHP();
        if (settings.isCustomDamageEnabled()) {
            updateCustomDamage();
        }
    }

    private void updateCustomSetting(int slotOffset, boolean isHealth) {
        for (int i = 2; i < 9; i++) {
            inventory.setItem(i + slotOffset, null);
        }
        int start = 2;
        if (settings.lobby.getTotalPlayers() == 8) {
            start = 1;
        }
        for (int i = 0; i < settings.team1.size(); i++) {
            BotBowsPlayer p = settings.team1.getPlayer(i);
            ItemStack item = p.avatar.getHeadItem();
            item.setAmount(isHealth ? p.getMaxHP() : p.getAttackDamage());
            inventory.setItem(i + start + slotOffset, item);
        }
        for (int i = 0; i < settings.team2.size(); i++) {
            BotBowsPlayer p = settings.team2.getPlayer(i);
            ItemStack item = p.avatar.getHeadItem();
            item.setAmount(isHealth ? p.getMaxHP() : p.getAttackDamage());
            inventory.setItem(8 - i + slotOffset, item);
        }
    }

    public void updateHP() {
        if (settings.isCustomHPEnabled()) {
            updateCustomSetting(0, true);
        } else {
            healthSlider.setProgressSlots(settings.getMaxHP());
        }
    }

    public void updateCustomDamage() {
        updateCustomSetting(9, false);
    }

    public void onCustomHPToggle() {
        if (settings.isCustomHPEnabled()) {
            inventory.setItem(0, CUSTOM_HP_ENABLED);
            inventory.setItem(1, VOID);
        } else {
            inventory.setItem(0, CUSTOM_HP_DISABLED);
            inventory.setItem(1, VOID);
            inventory.setItem(7, VOID);
            inventory.setItem(8, VOID);
        }
        updateHP();
    }

    public void onCustomDamageToggle() {
        if (settings.isCustomDamageEnabled()) {
            inventory.setItem(9, CUSTOM_DAMAGE_ENABLED);
            inventory.setItem(10, VOID);
            updateCustomDamage();
        } else {
            inventory.setItem(9, CUSTOM_DAMAGE_DISABLED);
            inventory.setItem(10, VOID);
            for (int i = 0; i < 7; i++) {
                inventory.setItem(11 + i, DISABLED_SLOT);
            }
        }
    }
}
