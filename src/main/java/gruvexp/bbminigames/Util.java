package gruvexp.bbminigames;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    public static ItemStack customHead(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        try {
            Class<?> profileClass = Class.forName("com.mojang.authlib.GameProfile");
            Object profile = profileClass.getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), null);

            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object texturesProperty = propertyClass
                    .getConstructor(String.class, String.class)
                    .newInstance("textures", base64);

            Object properties = profileClass.getMethod("getProperties").invoke(profile);
            properties.getClass().getMethod("put", Object.class, Object.class)
                    .invoke(properties, "textures", texturesProperty);

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public static Axis getAxis(BlockFace face) {
        int x = face.getModX();
        int y = face.getModY();
        int z = face.getModZ();

        if (x != 0 && y == 0 && z == 0) return Axis.X;
        if (y != 0 && x == 0 && z == 0) return Axis.Y;
        if (z != 0 && x == 0 && y == 0) return Axis.Z;

        return null;
    }

    public static Set<Location> getOrthogonalLocations(Location center, BlockFace direction) {
        Axis axis = getAxis(direction);

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
}
