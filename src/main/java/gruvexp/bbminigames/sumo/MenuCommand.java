package gruvexp.bbminigames.sumo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender; // grabs which player did the command. endrer datatype til Player
        Inventory inventory = Bukkit.createInventory(p, 18,ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Menu");

        //adder noen items
        ItemStack titleItem = new ItemStack(Material.STICK);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName(ChatColor.AQUA.toString() + ChatColor.BOLD + "SUMO");
        titleItem.setItemMeta(titleMeta);

        //Tournaments
        ItemStack tournamentItem = new ItemStack(Material.TUBE_CORAL_FAN);
        ItemMeta tournamentMeta = tournamentItem.getItemMeta();
        tournamentMeta.setDisplayName(ChatColor.GREEN + "Tournaments");
        tournamentMeta.setLore(new ArrayList<>(Arrays.asList("Everybody plays against", "everyone, and the one", "with most points win")));
        tournamentItem.setItemMeta(tournamentMeta);

        //golden helmet
        ItemStack crownItem = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta crownMeta = crownItem.getItemMeta();
        crownMeta.setDisplayName(ChatColor.GOLD + "Crown");
        crownMeta.setLore(new ArrayList<>(Arrays.asList("The one who survives the", "longest with the crown, wins")));
        crownItem.setItemMeta(crownMeta);


        inventory.setItem(4, titleItem);
        inventory.setItem(11, tournamentItem);
        inventory.setItem(15, crownItem);


        p.openInventory(inventory);
        return true;
    }
}
