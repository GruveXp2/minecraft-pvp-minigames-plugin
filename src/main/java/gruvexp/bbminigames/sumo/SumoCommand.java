package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SumoCommand implements CommandExecutor {
    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) { //starter spillet

        Player p = (Player) sender;
        String oper = args[0];
        if (Objects.equals(oper, "start")) {
            if (SumoManager.activeBattle == 1) {
                p.sendMessage(Component.text("There is already an active battle!", NamedTextColor.RED));
                return true;
            }

            int players = SumoData.playerList.size();
            if (players < 2) {
                p.sendMessage(Component.text("Not enough players! You need " + (2 - players) + " more.", NamedTextColor.RED));
            } else {
                SumoData.init();
                p.sendMessage("Starting tournament " + players + "v" + players);
                Bar.CreateBar("Game starting soon!_", BarColor.GREEN, BarStyle.SEGMENTED_12, 0);
                new PreBattle().runTaskTimer(Main.getPlugin(), 0L, 1L); //period 2T
            }
        } else {
            p.sendMessage(Component.text("invalid operation! Only valid operation at the moment is ")
                    .append(Component.text("sumo", NamedTextColor.AQUA, TextDecoration.ITALIC)));
        }
        return true;
    }
}
