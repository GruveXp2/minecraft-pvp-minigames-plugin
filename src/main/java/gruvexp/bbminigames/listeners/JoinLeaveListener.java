package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.getInventory().getItemInMainHand().getType() != Material.AIR) { // dropper itemet de hadde fra før av så det ikke blir sletta
            Main.WORLD.dropItem(p.getLocation(), p.getInventory().getItemInMainHand());
        }
        p.getInventory().setItemInMainHand(Menu.makeItem(Material.COMPASS, Component.text("Menu", NamedTextColor.LIGHT_PURPLE)));
        p.sendMessage(Component.text("Welcome to BotBows!", NamedTextColor.GREEN, TextDecoration.BOLD));
        p.sendMessage(Component.text("To join a game, run ")
                .append(Component.text("/menu ", NamedTextColor.AQUA))
                .append(Component.text("or right click the compass\n"))
                .append(Component.text("To leave a game, run "))
                .append(Component.text("/leave\n", NamedTextColor.AQUA))
                .append(Component.text("To access settings for a game, run "))
                .append(Component.text("/settings\n", NamedTextColor.AQUA))
                .append(Component.text("To start/stop a game, run "))
                .append(Component.text("/start ", NamedTextColor.AQUA))
                .append(Component.text("or "))
                .append(Component.text("/stopgame", NamedTextColor.AQUA)));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby != null) {
            lobby.leaveGame(p);
        }
    }

}
