package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import gruvexp.bbminigames.twtClassic.botbowsGames.GrautWackyGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BotBows {

    public static final ItemStack BOTBOW = getBotBow();
    public static Settings settings;
    public static BotBowsGame botBowsGame;
    private static final HashMap<Player, BotBowsPlayer> PLAYERS = new HashMap<>(); // liste med alle players som er i gamet
    public static boolean activeGame = false; // hvis spillet har starta, så kan man ikke gjøre ting som /settings

    public static GameMenu gameMenu;

    public static void init() { // a
        settings = new Settings();
        settings.initMenus();
        gameMenu = new GameMenu();
    }

    public static void joinGame(Player p) {
        if (activeGame) {
            p.sendMessage(Component.text("A game is already ongoing, wait until it ends before you join", NamedTextColor.RED));
            return;
        }
        settings.joinGame(p);
    }

    public static void leaveGame(Player p) {
        BotBowsPlayer bp = getBotBowsPlayer(p);
        if (!settings.isPlayerJoined(p)) {
            p.sendMessage("Nothing happened, you werent in the game in the first place");
            return;
        }
        if (activeGame) {
            botBowsGame.leaveGame(bp);
        } else {
            settings.leaveGame(bp);
        }
    }

    public static void startGame(Player gameStarter) {
        if (activeGame) {
            gameStarter.sendMessage(Component.text("The game has already started!", NamedTextColor.RED));
            return;
        } else if (settings.team1.isEmpty() || settings.team2.isEmpty()) {
            gameStarter.sendMessage(Component.text("Cant start game, both teams must have at least 1 player each", NamedTextColor.RED));
            return;
        }
        if (settings.currentMap == BotBowsMap.GRAUT_VS_WACKY) {
            botBowsGame = new GrautWackyGame(settings);
        } else {
            botBowsGame = new BotBowsGame(settings);
        }
        botBowsGame.startGame(gameStarter);
    }

    private static ItemStack getBotBow() {
        ItemStack BOTBOW = new ItemStack(Material.CROSSBOW);
        CrossbowMeta meta = (CrossbowMeta) BOTBOW.getItemMeta();
        meta.displayName(Component.text("BotBow").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text("The strongest bow"), Component.text("ever known to man")));
        meta.addEnchant(Enchantment.POWER, 10, true);
        meta.addEnchant(Enchantment.PUNCH, 10, true);
        meta.addChargedProjectile(new ItemStack(Material.ARROW));
        Damageable damageable = (Damageable) meta;
        damageable.setDamage((short) 464);
        BOTBOW.setItemMeta(damageable);
        return BOTBOW;
    }

    public static BotBowsPlayer getBotBowsPlayer(Player p) {
        return PLAYERS.get(p);
    }

    public static void registerBotBowsPlayer(BotBowsPlayer p) {
        if (PLAYERS.containsKey(p.player)) return;
        PLAYERS.put(p.player, p);
    }

    public static void check4Victory(BotBowsPlayer dedPlayer) {
        botBowsGame.check4Victory(dedPlayer);
    }

    public static void messagePlayers(Component message) {
        for (BotBowsPlayer p : settings.getPlayers()) {
            p.player.sendMessage(message);
        }
    }

    public static void debugMessage(String message) {
        if (!TestCommand.debugging) return;
        messagePlayers(Component.text("[DEBUG]: " + message, NamedTextColor.GRAY));
        Main.getPlugin().getLogger().info("[DEBUG]: " + message);
    }

    public static void debugMessage(String message, boolean showMessage) {
        if (showMessage) debugMessage(message);
    }

    public static void titlePlayers(String title, int duration) {
        for (BotBowsPlayer p : settings.getPlayers()) {
            p.player.sendTitle(title, null, 2, duration, 5);
        }
    }

    public static int getTotalPlayers() {
        return settings.getPlayers().size();
    }
    public static Collection<BotBowsPlayer> getPlayers() {
        return PLAYERS.values();
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