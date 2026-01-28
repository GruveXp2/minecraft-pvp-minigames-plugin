package gruvexp.bbminigames;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.joml.Vector3i;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Util {

    public static Location toLocation(World world, String x, String y, String z) {
        try {
            return new Location(world, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ChatColor.RED + "" + x + " " + y + " " + z + " is not a valid position!");
        }
    }

    public static Vector3i getTargetBlockLoc(Player player, int range) { // modified method by https://www.spigotmc.org/members/clip.1001/ that gets the block the player is looking at
        Block lastBlock = getTargetBlock(player, range);
        return new Vector3i(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ());
    }

    public static Block getTargetBlock(Player player, int range) { // modified method by https://www.spigotmc.org/members/clip.1001/ that gets the block the player is looking at
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return lastBlock;
    }

    public static String print(Vector3i vec) {
        return vec.x + " " + vec.y + " " + vec.z;
    }

    public static String print(Vector vec) {
        return String.format("%.1f %.1f %.1f", vec.getX(), vec.getY(), vec.getZ());
    }

    public static ItemStack playerHead(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        meta.setOwningPlayer(player);
        skull.setItemMeta(meta);
        return skull;
    }

    /*private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile("RANDOM_UUID"); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url); // The URL to the skin, for example: https://textures.minecraft.net/texture/18813764b2abc94ec3c3bc67b9147c21be850cdf996679703157f4555997ea63a
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
    }*/

    public static Axis getAxis(BlockFace face) {
        int x = face.getModX();
        int y = face.getModY();
        int z = face.getModZ();

        if (x != 0 && y == 0 && z == 0) return Axis.X;
        if (y != 0 && x == 0 && z == 0) return Axis.Y;
        if (z != 0 && x == 0 && y == 0) return Axis.Z;

        return null;
    }

    public static Set<Location> getOrthogonalLocations(Location center, Axis axis) {

        Set<BlockFace> orthogonals = EnumSet.noneOf(BlockFace.class);
        for (BlockFace face : BlockFace.values()) {
            Axis faceAxis = getAxis(face);
            if (faceAxis != null && faceAxis != axis) {
                orthogonals.add(face);
            }
        }
        Set<Location> locations = new HashSet<>();
        orthogonals.forEach(face -> locations.add(center.clone().add(face.getDirection())));
        return locations;
    }

    public static Set<Chunk> getChunksAround(Location location, int blockRadius) {
        Set<Chunk> chunks = new HashSet<>();
        World world = location.getWorld();

        int minX = (location.getBlockX() - blockRadius) >> 4;
        int maxX = (location.getBlockX() + blockRadius) >> 4;
        int minZ = (location.getBlockZ() - blockRadius) >> 4;
        int maxZ = (location.getBlockZ() + blockRadius) >> 4;

        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                chunks.add(world.getChunkAt(chunkX, chunkZ));
            }
        }
        return chunks;
    }
}
