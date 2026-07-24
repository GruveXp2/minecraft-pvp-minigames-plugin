package gruvexp.bbminigames.api.ability;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Projectile;

public interface AbilityContext {
    record EntityPlace(Location loc) {}
    record Melee(BotBowsPlayer defender) {}
    record Launch(Projectile projectile) {}
    record BlockPlace(Block block, BlockFace face) {}
}
