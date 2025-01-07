package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyMenu extends Menu {

    @Override
    public Component getMenuName() {
        return Component.text("Join Lobby");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;
        Player p = (Player) e.getWhoClicked();
        switch (clickedItem.getType()) {
            case LIGHT_GRAY_CONCRETE, LIME_CONCRETE, YELLOW_CONCRETE -> {
                ItemMeta meta = clickedItem.getItemMeta();
                Component displayName = meta.displayName();
                assert displayName != null;
                String text = PlainTextComponentSerializer.plainText().serialize(displayName);
                int lobbyID = Integer.parseInt(String.valueOf(text.charAt(text.length() - 1)));
                BotBows.getLobby(lobbyID).joinGame(p);
            }
        }
    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < 3; i++) {
            inventory.setItem(i, VOID);
            inventory.setItem(i + 6, VOID);
        }
    }

    public void updateLobbyItem(Lobby lobby) {
        ItemStack lobbyItem;
        if (lobby.isGameActive()) {
            lobbyItem = makeItem(Material.RED_CONCRETE, Component.text("Lobby #" + lobby.ID),
                    Component.text("Closed: active game"));
        } else {
            lobbyItem = switch (lobby.getTotalPlayers()) {
                case 0 -> makeItem(Material.LIGHT_GRAY_CONCRETE, Component.text("Lobby #" + lobby.ID),
                        Component.text("No players"));
                case 1, 2, 3, 4, 5 -> makeItem(Material.LIME_CONCRETE, Component.text("Lobby #" + lobby.ID),
                        Component.text(lobby.getTotalPlayers() + "/8 players"));
                case 6, 7 -> makeItem(Material.YELLOW_CONCRETE, Component.text("Lobby #" + lobby.ID),
                        Component.text(lobby.getTotalPlayers() + "/8 players"));
                default -> makeItem(Material.ORANGE_CONCRETE, Component.text("Lobby #" + lobby.ID),
                        Component.text("FULL"));
            };
        }
        inventory.setItem(2 + lobby.ID, lobbyItem);
    }
}
