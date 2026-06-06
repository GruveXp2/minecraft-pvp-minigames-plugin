package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class TNTHandler implements Listener{
    public TNTHandler(Main plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onTNTPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() == Material.TNT) {
            Player p = e.getPlayer();
            Main.getPlugin().getLogger().info(p.getName() + " tried to place a TNT, but failed miserably");
            e.setCancelled(true);
        }
    }
}
