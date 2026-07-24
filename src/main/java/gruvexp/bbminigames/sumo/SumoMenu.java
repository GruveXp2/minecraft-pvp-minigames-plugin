package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.menu.Menu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class SumoMenu extends Menu {

    public SumoMenu() {
        inventory.setItem(2, TOURNAMENTS);
        inventory.setItem(6, CROWN);
    }

    ItemStack TOURNAMENTS = Menu.makeItem(Material.TUBE_CORAL_FAN, Component.text("Tournaments"),
            Component.text("Everybody plays against"), Component.text("everyone, and the one"), Component.text("with most points win"));

    ItemStack CROWN = Menu.makeItem(Material.GOLDEN_HELMET, Component.text("Crown"),
            Component.text("The one who survives the"), Component.text("longest with the crown, wins"));

    @Override
    public Component getMenuName() {
        return Component.text("Select sumo gamemode");
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
        if (clickedItem.getType() == Material.TUBE_CORAL_FAN) {
            if (SumoData.playerList.contains(p)) {
                p.sendMessage(Component.text("Nothing happened, you already joined. Ther are currently " + SumoData.playerList.size() + " players.", NamedTextColor.RED));
                return;
            } //cant join the same game many times
            p.sendMessage(Component.text("Joining SUMO Tournaments", NamedTextColor.GREEN));
            p.teleport(new Location(p.getWorld(), 29.3, 36.0, -174.3));
            p.setGameMode(GameMode.ADVENTURE);
            SumoData.playerList.add(p);

            for (Player q : Bukkit.getOnlinePlayers()) {
                q.sendMessage(Component.text("", NamedTextColor.YELLOW)
                        .append(p.name())
                        .append(Component.text(" joined Sumo! (" + SumoData.playerList.size() + ")")));
            }
            p.sendMessage(Component.text("Current players: ")
                    .append(Component.text(SumoData.playerList.stream()
                            .map(Player::getName).collect(Collectors.joining(", ")), NamedTextColor.GREEN)));
        } else if (clickedItem.getType() == Material.GOLDEN_HELMET) {
            p.sendMessage(Component.text("Joining SUMO Crown", NamedTextColor.GREEN));
            p.teleport(new Location(p.getWorld(), 29.3, 36.0, -174.3));
        }
    }
}
