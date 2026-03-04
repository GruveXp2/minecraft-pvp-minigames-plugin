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

    public static boolean isBlockMiddleTransparent(Material material) {
        return switch (material) {
            //<editor-fold defaultstate="collapsed" desc="isTransparent">
            // Start generate - Material#isTransparent
            case ACACIA_BUTTON:
            case ACACIA_SAPLING:
            case ACTIVATOR_RAIL:
            case AIR:
            case ALLIUM:
            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case AZURE_BLUET:
            case BEETROOTS:
            case BIRCH_BUTTON:
            case BIRCH_SAPLING:
            case BLACK_CARPET:
            case BLUE_CARPET:
            case BLUE_ORCHID:
            case BROWN_CARPET:
            case BROWN_MUSHROOM:
            case CARROTS:
            case CAVE_AIR:
            case CHORUS_FLOWER:
            case CHORUS_PLANT:
            case COCOA:
            case COMPARATOR:
            case CREEPER_HEAD:
            case CREEPER_WALL_HEAD:
            case CYAN_CARPET:
            case DANDELION:
            case DARK_OAK_BUTTON:
            case DARK_OAK_SAPLING:
            case DEAD_BUSH:
            case DETECTOR_RAIL:
            case END_PORTAL:
            case FERN:
            case FIRE:
            case FLOWER_POT:
            case GRAY_CARPET:
            case GREEN_CARPET:
            case JUNGLE_BUTTON:
            case JUNGLE_SAPLING:
            case LADDER:
            case LARGE_FERN:
            case LEVER:
            case LIGHT_BLUE_CARPET:
            case LIGHT_GRAY_CARPET:
            case LILAC:
            case LILY_PAD:
            case LIME_CARPET:
            case MAGENTA_CARPET:
            case MELON_STEM:
            case NETHER_PORTAL:
            case NETHER_WART:
            case OAK_BUTTON:
            case OAK_SAPLING:
            case ORANGE_CARPET:
            case ORANGE_TULIP:
            case OXEYE_DAISY:
            case PEONY:
            case PINK_CARPET:
            case PINK_TULIP:
            case PLAYER_HEAD:
            case PLAYER_WALL_HEAD:
            case POPPY:
            case POTATOES:
            case POTTED_ACACIA_SAPLING:
            case POTTED_ALLIUM:
            case POTTED_AZALEA_BUSH:
            case POTTED_AZURE_BLUET:
            case POTTED_BIRCH_SAPLING:
            case POTTED_BLUE_ORCHID:
            case POTTED_BROWN_MUSHROOM:
            case POTTED_CACTUS:
            case POTTED_DANDELION:
            case POTTED_DARK_OAK_SAPLING:
            case POTTED_DEAD_BUSH:
            case POTTED_FERN:
            case POTTED_FLOWERING_AZALEA_BUSH:
            case POTTED_JUNGLE_SAPLING:
            case POTTED_OAK_SAPLING:
            case POTTED_ORANGE_TULIP:
            case POTTED_OXEYE_DAISY:
            case POTTED_PINK_TULIP:
            case POTTED_POPPY:
            case POTTED_RED_MUSHROOM:
            case POTTED_RED_TULIP:
            case POTTED_SPRUCE_SAPLING:
            case POTTED_WHITE_TULIP:
            case POWERED_RAIL:
            case PUMPKIN_STEM:
            case PURPLE_CARPET:
            case RAIL:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case RED_CARPET:
            case RED_MUSHROOM:
            case RED_TULIP:
            case REPEATER:
            case ROSE_BUSH:
            case SHORT_GRASS:
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
            case SNOW:
            case SPRUCE_BUTTON:
            case SPRUCE_SAPLING:
            case STONE_BUTTON:
            case STRUCTURE_VOID:
            case SUGAR_CANE:
            case SUNFLOWER:
            case TALL_GRASS:
            case TORCH:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case VINE:
            case VOID_AIR:
            case WALL_TORCH:
            case WHEAT:
            case WHITE_CARPET:
            case WHITE_TULIP:
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
            case YELLOW_CARPET:
            case ZOMBIE_HEAD:
            case ZOMBIE_WALL_HEAD:
                yield true;
            default:
                yield false;
        };
    }
}
