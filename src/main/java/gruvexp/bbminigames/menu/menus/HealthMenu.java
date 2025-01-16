package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.MenuSlider;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class HealthMenu extends SettingsMenu {

    private static final ItemStack CUSTOM_HP_DISABLED = makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.RED),
            Component.text("Disabled", NamedTextColor.RED),
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp"));

    private static final ItemStack CUSTOM_HP_ENABLED = makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.GREEN),
            Component.text("Enabled", NamedTextColor.GREEN),
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp"));

    private boolean customHP;
    private MenuSlider healthSlider;

    public HealthMenu(Settings settings) {
        super(settings);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Health (3/6)");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker)) && !clickedOnBottomButtons(e)) return;

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
                    enableCustomHP();
                }
            }
            case LIME_STAINED_GLASS_PANE -> {
                if (e.getCurrentItem().equals(CUSTOM_HP_ENABLED)) {
                    disableCustomHP();
                }
            }
            case PLAYER_HEAD -> {
                ItemStack head = e.getCurrentItem();
                Player p = Bukkit.getPlayer(UUID.fromString(head.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING)));
                BotBowsPlayer bp = settings.lobby.getBotBowsPlayer(p);
                int slot = e.getSlot();
                int maxHP = head.getAmount();

                if (maxHP > 9) {
                    maxHP += 5;
                    if (maxHP > 20) {
                        maxHP = 1;
                    }
                } else {
                    maxHP += 1;
                }

                head.setAmount(maxHP);
                inventory.setItem(slot, head);
                bp.setMaxHP(maxHP);
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == getSlots() - 6) {
                    settings.teamsMenu.open(clicker);
                } else if (e.getSlot() == getSlots() - 4) {
                    settings.winConditionMenu.open(clicker);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        healthSlider = new MenuSlider(inventory, 11, Material.PINK_STAINED_GLASS_PANE, NamedTextColor.RED, List.of("1", "2", "3", "4", "5"));
        setPageButtons(2, true, true);
        setFillerVoid();
    }

    public void updateMenu() {
        if (customHP) { // each player can have their own health
            for (int i = 9; i < 18; i++) {
                inventory.setItem(i, null);
            }
            for (int i = 0; i < settings.team1.size(); i++) {
                BotBowsPlayer p = settings.team1.getPlayer(i);
                ItemStack item = makeHeadItem(p.player, settings.team1.color);
                item.setAmount(p.getMaxHP());
                inventory.setItem(i + 9, item);
            }
            for (int i = 0; i < settings.team2.size(); i++) {
                BotBowsPlayer p = settings.team2.getPlayer(i);
                ItemStack item = makeHeadItem(p.player, settings.team2.color);
                item.setAmount(p.getMaxHP());
                inventory.setItem(17 - i, item);
            }
        } else { // The normal menu with a slider
            healthSlider.setProgressSlots(settings.getMaxHP());
        }
    }
    
    public void enableCustomHP() {
        customHP = true;
        inventory.setItem(6, CUSTOM_HP_ENABLED);
        inventory.setItem(13, VOID);
        updateMenu();
    }

    public void disableCustomHP() {
        customHP = false;
        inventory.setItem(6, CUSTOM_HP_DISABLED);
        inventory.setItem(9, VOID);
        inventory.setItem(10, VOID);
        inventory.setItem(16, VOID);
        inventory.setItem(17, VOID);
        settings.setMaxHP(3);
    }
}
