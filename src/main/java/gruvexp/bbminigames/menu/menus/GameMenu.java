package gruvexp.bbminigames.menu.menus;

import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GameMenu extends Menu {
    @Override
    public Component getMenuName() {
        return Component.text("Game Menu");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getCurrentItem().getType() == Material.BOW) {
            BotBows.lobbyMenu.open(p);
        } else if (e.getCurrentItem().getType() == Material.STICK) {
            p.sendMessage(Component.text("This will be added soon. You can take a look at the arena ")
                    .append(Component.text("here", Style.style(NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s 38.0 31.3 -176.5 102 22"))));
        }
    }

    @Override
    public void setMenuItems() {
        ItemStack botbows = makeItem(Material.BOW, Component.text("BotBows Classic"), Component.text("The classic game of BotBows"));
        ItemStack sumo = makeItem(Material.STICK, Component.text("Sumo"), Component.text("The Sumo minigame"));
        inventory.setItem(3, botbows);
        inventory.setItem(5, sumo);
        setFillerVoid();
    }
}
