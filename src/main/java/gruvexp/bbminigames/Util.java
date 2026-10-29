package gruvexp.bbminigames;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;
import org.joml.Vector3i;

public class Util {

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

    public static boolean isBlockMiddleTransparent(Material material) {
        return switch (material) {
            case ACACIA_BUTTON, ACACIA_SAPLING, ACTIVATOR_RAIL, AIR, ALLIUM, ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM,
                 AZURE_BLUET, BEETROOTS, BIRCH_BUTTON, BIRCH_SAPLING, BLACK_CARPET, BLUE_CARPET, BLUE_ORCHID,
                 BROWN_CARPET, BROWN_MUSHROOM, CARROTS, CAVE_AIR, CHORUS_FLOWER, CHORUS_PLANT, COCOA, COMPARATOR,
                 CREEPER_HEAD, CREEPER_WALL_HEAD, CYAN_CARPET, DANDELION, DARK_OAK_BUTTON, DARK_OAK_SAPLING, DEAD_BUSH,
                 DETECTOR_RAIL, END_PORTAL, FERN, FIRE, FLOWER_POT, GRAY_CARPET, GREEN_CARPET, JUNGLE_BUTTON,
                 JUNGLE_SAPLING, LADDER, LARGE_FERN, LEVER, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LILAC, LILY_PAD,
                 LIME_CARPET, MAGENTA_CARPET, MELON_STEM, NETHER_PORTAL, NETHER_WART, OAK_BUTTON, OAK_SAPLING,
                 ORANGE_CARPET, ORANGE_TULIP, OXEYE_DAISY, PEONY, PINK_CARPET, PINK_TULIP, PLAYER_HEAD,
                 PLAYER_WALL_HEAD, POPPY, POTATOES, POTTED_ACACIA_SAPLING, POTTED_ALLIUM, POTTED_AZALEA_BUSH,
                 POTTED_AZURE_BLUET, POTTED_BIRCH_SAPLING, POTTED_BLUE_ORCHID, POTTED_BROWN_MUSHROOM, POTTED_CACTUS,
                 POTTED_DANDELION, POTTED_DARK_OAK_SAPLING, POTTED_DEAD_BUSH, POTTED_FERN, POTTED_FLOWERING_AZALEA_BUSH,
                 POTTED_JUNGLE_SAPLING, POTTED_OAK_SAPLING, POTTED_ORANGE_TULIP, POTTED_OXEYE_DAISY, POTTED_PINK_TULIP,
                 POTTED_POPPY, POTTED_RED_MUSHROOM, POTTED_RED_TULIP, POTTED_SPRUCE_SAPLING, POTTED_WHITE_TULIP,
                 POWERED_RAIL, PUMPKIN_STEM, PURPLE_CARPET, RAIL, REDSTONE_TORCH, REDSTONE_WALL_TORCH, REDSTONE_WIRE,
                 RED_CARPET, RED_MUSHROOM, RED_TULIP, REPEATER, ROSE_BUSH, SHORT_GRASS, SKELETON_SKULL,
                 SKELETON_WALL_SKULL, SNOW, SPRUCE_BUTTON, SPRUCE_SAPLING, STONE_BUTTON, STRUCTURE_VOID, SUGAR_CANE,
                 SUNFLOWER, TALL_GRASS, TORCH, TRIPWIRE, TRIPWIRE_HOOK, VINE, VOID_AIR, WALL_TORCH, WHEAT, WHITE_CARPET,
                 WHITE_TULIP, WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL, YELLOW_CARPET, ZOMBIE_HEAD,
                 ZOMBIE_WALL_HEAD -> true;
            default -> false;
        };
    }
}
