package gruvexp.bbminigames.extras;

import gruvexp.bbminigames.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class StickSlap {

    public static int cooldown = 20; // 20 ticks cooldown but can be changed with commands

    public static void handleHit(Player attacker) {
        PlayerInventory atkInv = attacker.getInventory();
        if (atkInv.getItemInMainHand().getType() == Material.BLAZE_ROD) { // hvis man bruker blaze rod
            Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Consumer<>() { // loop x antall ganger hver tick

                int counter = cooldown;
                @Override
                public void accept(BukkitTask task) {
                    if (counter == 0) { // når antall sticks er 0 så settes en blaze rod istedet og loopsn stoppes
                        atkInv.setItemInMainHand(new ItemStack(Material.BLAZE_ROD));
                        task.cancel();
                        counter--;
                        return;
                    }
                    atkInv.setItemInMainHand(new ItemStack(Material.STICK, counter)); // setter en stick, antall sticks går ned hver tick
                    counter--;
                }
            }, 0, 1);
        }
    }
}
