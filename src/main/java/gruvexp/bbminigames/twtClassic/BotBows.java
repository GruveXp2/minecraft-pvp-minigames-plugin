package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.menus.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

public class BotBows {

    public static final ItemStack BOTBOW = getBotBow();
    private static Lobby[] lobbies;
    private static final HashMap<Player, Lobby> players = new HashMap<>(); // liste med alle players som er i gamet

    public static GameMenu gameMenu;
    public static LobbyMenu lobbyMenu;

    public static void init() { // a
        gameMenu = new GameMenu();
        lobbyMenu = new LobbyMenu();
        lobbies = new Lobby[]{new Lobby(1), new Lobby(2), new Lobby(3)};
    }

    public static Lobby getLobby(int ID) {
        return lobbies[ID];
    }

    public static Lobby getLobby(Player p) {
        return players.get(p);
    }

    public static Lobby[] getLobbies() {
        return lobbies;
    }

    public static boolean isPlayerJoined(Player p) {
        return getLobby(p) != null;
    }

    private static ItemStack getBotBow() {
        ItemStack botBow = new ItemStack(Material.CROSSBOW);
        CrossbowMeta meta = (CrossbowMeta) botBow.getItemMeta();
        meta.displayName(Component.text("BotBow").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text("The strongest bow"), Component.text("ever known to man")));
        meta.addEnchant(Enchantment.POWER, 10, true);
        meta.addEnchant(Enchantment.PUNCH, 10, true);
        meta.addChargedProjectile(new ItemStack(Material.ARROW));
        Damageable damageable = (Damageable) meta;
        damageable.setDamage((short) 464);
        botBow.setItemMeta(damageable);
        return botBow;
    }

    public static void debugMessage(String message) {
        if (!TestCommand.debugging) return;
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("[DEBUG]: " + message, NamedTextColor.GRAY)));
        Main.getPlugin().getLogger().info("[DEBUG]: " + message);
    }

    public static void debugMessage(String message, boolean showMessage) {
        if (showMessage) debugMessage(message);
    }

    public static void handleMovement(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        boolean b = TestCommand.verboseDebugging;
        Block block = p.getLocation().add(0, -0.05, 0).getBlock(); // sjekker rett under, bare 0.05 itilfelle det er teppe

        BotBows.debugMessage("1: Material: " + block.getType().name(), b);
        if (block.getType() == Material.AIR) {
            block = p.getLocation().add(0, -0.9, 0).getBlock(); // hvis man står på kanten av et teppe kan det være en effektblokk under
            BotBows.debugMessage("2: Material: " + block.getType().name(), b);
        }
        if (block.getType() == Material.AIR) {
            block = p.getLocation().add(0, 0, 0).getBlock();
            BotBows.debugMessage("3: Material: " + block.getType().name(), b);
        } else {
            BotBows.debugMessage("Material: " + block.getType().name(), b);
        }
        Material material = block.getType();
        if (block.getType() == Material.LIGHT) {
            BotBows.debugMessage("Light level: " + ((Light) block.getBlockData()).getLevel(), b);
            if (((Light) block.getBlockData()).getLevel() == 0) { // sida det ikke går an å sjekke når players står uttafor kanten, så workarounder jeg det ved å sette light bloccs ved sida cyan yeetpads
                material = Material.CYAN_CARPET;
                BotBows.debugMessage("yee it works", b);
            } else if (((Light) block.getBlockData()).getLevel() == 1) {
                return;
            }
        }
        switch (material) { // add effekter basert på åssen blokk som er under
            case YELLOW_CONCRETE, YELLOW_CONCRETE_POWDER, YELLOW_CARPET -> p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 1200, 6, true, false));
            case CYAN_CONCRETE, CYAN_CONCRETE_POWDER, CYAN_CARPET -> {
                double Δy = e.getTo().getY() - e.getFrom().getY();
                if (Δy <= 0.1) {break;} // fortsett bare viss man har hoppa (et visst antall upwards momentum)
                double vX = p.getLocation().getDirection().getX();
                double vZ = p.getLocation().getDirection().getZ();

                p.setVelocity(new Vector(vX*2.5, 0.5, vZ*2.5));
                p.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 10, 2);
            }
            default -> p.removePotionEffect(PotionEffectType.JUMP_BOOST);
        }
    }
}