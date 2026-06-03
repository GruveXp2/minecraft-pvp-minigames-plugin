package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.SettingsMenu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.UUID;

public class TeamsMenu extends SettingsMenu {

    public static final ItemStack SWITCH_SIDE = makeItem("switch", Component.text("Switch sides", NamedTextColor.LIGHT_PURPLE), Component.text("switches the teams to be their other"), Component.text("so the teams spawn on the opposite side"));

    BotBowsTeam team1;
    BotBowsTeam team2;

    public TeamsMenu(Settings settings) {
        super(settings);
        for (int i = 2; i < 7; i++) {
            inventory.setItem(i, null);
            inventory.setItem(i + 9, null);
        }
        setPageButtons(2, true, true);
        inventory.setItem(22, SWITCH_SIDE);
    }

    @Override
    public Component getMenuName() {
        return Component.text("Teams (2/6)");
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // if you click on a player then they change teams
        Player clicker = (Player) e.getWhoClicked();
        BotBowsPlayer bp = BotBows.getBotBowsPlayer(clicker);
        if (e.getClickedInventory() != inventory) return;
        if (handlePageClick(e)) return;
        if (!settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return;

        switch (e.getCurrentItem().getType()) {
            case PLAYER_HEAD -> {
                NamespacedKey key = new NamespacedKey(Main.getPlugin(), "uuid");
                UUID playerId = UUID.fromString(Objects.requireNonNull(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                BotBowsPlayer headBp = settings.lobby.getBotBowsPlayer(playerId);
                headBp.getTeam().getOppositeTeam().join(headBp);
                recalculateTeam();
                settings.healthMenu.updateMenu(); // pga teammembers endres må health settings oppdateres pga det er basert på farger
            }
            case FIREWORK_STAR -> {
                if (e.getSlot() == 22) {
                    settings.switchTeamSides();
                }
            }
        }
    }

    @Override
    public void prevPage(Player p) {
        settings.mapMenus.get(BotBows.getBotBowsPlayer(p)).open(p);
    }

    @Override
    public void nextPage(Player p) {
        settings.healthMenu.open(p);
    }

    public void registerTeams() {
        team1 = settings.team1;
        team2 = settings.team2;
        setColoredGlassPanes();
    }

    private void setColoredGlassPanes() { // update the glass pane items that show the team colors and name
        ItemStack team1Pane = makeItem(team1.getGlassPane(), Component.text("Team " + team1.name, team1.color));
        ItemStack team2Pane = makeItem(team2.getGlassPane(), Component.text("Team " + team2.name, team2.color));
        inventory.setItem(0, team1Pane);
        inventory.setItem(1, team1Pane);
        inventory.setItem(7, team1Pane);
        inventory.setItem(8, team1Pane);
        inventory.setItem(9, team2Pane);
        inventory.setItem(10, team2Pane);
        inventory.setItem(16, team2Pane);
        inventory.setItem(17, team2Pane);
    }

    public void recalculateTeam() {
        inventory.remove(Material.PLAYER_HEAD); // Fjerner player heads sånn at det kan kalkuleres pånytt

        for (int i = 0; i < team1.size(); i++) { // team 1
            ItemStack playerHead = team1.getPlayer(i).avatar.getHeadItem();
            inventory.setItem(2 + i, playerHead);
        }
        for (int i = 0; i < team2.size(); i++) { // team 2
            ItemStack playerHead = team2.getPlayer(i).avatar.getHeadItem();
            inventory.setItem(11 + i, playerHead);
        }
    }
}
