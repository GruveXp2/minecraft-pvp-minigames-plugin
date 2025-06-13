package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.menu.menus.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BotBows {

    public static final ItemStack BOTBOW = getBotBow();
    private static Lobby[] lobbies;
    private static final HashMap<Player, Lobby> players = new HashMap<>(); // liste med alle players som er i gamet

    public static GameMenu gameMenu;
    public static LobbyMenu lobbyMenu;

    public static ItemStack MENU_ITEM = Menu.makeItem(Material.COMPASS, Component.text("Menu", NamedTextColor.LIGHT_PURPLE));
    public static ItemStack SETTINGS_ITEM = Menu.makeItem("gear", Component.text("Settings", NamedTextColor.LIGHT_PURPLE));

    public static int HIT_DISABLED_ITEM_TICKS = 40;
    public static final Random RANDOM = new Random();

    public static void init() { // a
        gameMenu = new GameMenu();
        lobbyMenu = new LobbyMenu();
        lobbies = new Lobby[]{new Lobby(0), new Lobby(1), new Lobby(2)};
    }

    public static void registerPlayerLobby(Player p, Lobby lobby) {
        players.put(p, lobby);
    }

    public static void unRegisterPlayerLobby(Player p) {
        players.remove(p);
    }

    public static Lobby getLobby(int ID) {
        return lobbies[ID];
    }

    public static Lobby getLobby(Player p) {
        return players.get(p);
    }

    public static BotBowsPlayer getBotBowsPlayer(Player p) { // gets the BotBowsPlayer that is used by the lobby the player is in
        Lobby lobby = getLobby(p);
        if (lobby == null) return null;
        return lobby.getBotBowsPlayer(p);
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

    public static void accessSettings(Player p) {
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) {
            p.sendMessage(Component.text("You have to join to access the settings", NamedTextColor.RED));
            return;
        }
        if (lobby.isGameActive()) {
            p.sendMessage(Component.text("Cant change settings, the game is already ongoing!", NamedTextColor.RED));
            return;
        }
        lobby.settings.mapMenu.open(p);
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
                material = Material.YELLOW_CARPET;
            } else if (((Light) block.getBlockData()).getLevel() == 2) {
                material = Material.AIR;
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

    public static void setTimeSmooth(long start, long end, int seconds) {
        int ticks = seconds * 20;
        long step = (end - start) / ticks;
        new BukkitRunnable() {
            long count = 0;
            public void run() {
                if (count >= ticks) cancel();
                else Main.WORLD.setTime(start + (count++ * step));
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static TextColor lighten(TextColor color, double factor) {
        int r = color.red();
        int g = color.green();
        int b = color.blue();

        r += (int)((255 - r) * factor);
        g += (int)((255 - g) * factor);
        b += (int)((255 - b) * factor);

        return TextColor.color(r, g, b);
    }
}