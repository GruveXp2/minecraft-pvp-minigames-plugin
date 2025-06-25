package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import gruvexp.bbminigames.twtClassic.botbowsGames.IcyRavineGame;
import gruvexp.bbminigames.twtClassic.botbowsGames.SpaceStationGame;
import gruvexp.bbminigames.twtClassic.botbowsGames.SteamPunkGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Lobby {

    public final int ID;
    private final HashMap<Player, BotBowsPlayer> players = new HashMap<>(); // liste med alle players som er i gamet
    public Settings settings;
    public BotBowsGame botBowsGame;
    private boolean activeGame = false; // hvis spillet har starta, så kan man ikke gjøre ting som /settings

    public static ItemStack READY = Menu.makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text("Ready", NamedTextColor.GREEN),
            Component.text("When everyone else is also ready, the match will start"),
            Component.text("To unready, right click this item"));

    public static ItemStack NOT_READY = Menu.makeItem(Material.RED_STAINED_GLASS_PANE, Component.text("Not Ready", NamedTextColor.RED),
            Component.text("The match will not start until youre ready"),
            Component.text("To ready up, right click this item"));

    public static ItemStack LOADING = Menu.makeItem(Material.YELLOW_STAINED_GLASS_PANE, Component.text("Loading...", NamedTextColor.YELLOW),
            Component.text("Please wait for your action to be processed"));

    public Lobby(int ID) {
        this.ID = ID;
        BotBows.lobbyMenu.updateLobbyItem(this);
        settings = new Settings(this);
        settings.initMenus();
    }


    public void joinGame(Player p) {
        if (activeGame) {
            p.sendMessage(Component.text("A game is already ongoing, wait until it ends before you join", NamedTextColor.RED));
            return;
        }
        if (BotBows.getLobby(p) != null) {
            if (BotBows.getLobby(p) == this) {
                p.sendMessage(Component.text("You already joined!", NamedTextColor.RED));
                return;
            }
            BotBows.getLobby(p).leaveGame(p); // leaver den forrige lobbien for å joine denne
        }
        settings.joinGame(p);
        BotBows.lobbyMenu.updateLobbyItem(this);
        BotBows.registerPlayerLobby(p, this);
        p.getInventory().setItem(0, BotBows.SETTINGS_ITEM);
        p.getInventory().setItem(4, NOT_READY);
    }

    public void leaveGame(Player p) {
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
        players.remove(p);
        BotBows.lobbyMenu.updateLobbyItem(this);
        BotBows.unRegisterPlayerLobby(p);
        Inventory inv = p.getInventory();
        for (int i = 1; i < 9; i++) {
            inv.setItem(i, null); // fjerner abilities
        }
    }

    public boolean isPlayerJoined(Player p) {
        return players.containsKey(p);
    }

    public BotBowsPlayer getBotBowsPlayer(Player p) {
        return players.get(p);
    }

    public void registerBotBowsPlayer(BotBowsPlayer p) {
        if (players.containsKey(p.player)) return;
        players.put(p.player, p);
    }

    public Collection<BotBowsPlayer> getPlayers() {
        return players.values();
    }

    public void startGame(Player gameStarter) {
        if (activeGame) {
            gameStarter.sendMessage(Component.text("The game has already started!", NamedTextColor.RED));
            return;
        } else if (settings.team1.isEmpty() || settings.team2.isEmpty()) {
            gameStarter.sendMessage(Component.text("Cant start game, both teams must have at least 1 player each", NamedTextColor.RED));
            return;
        }
        messagePlayers(Component.text(gameStarter.getName() + ": ", NamedTextColor.GRAY)
                .append(Component.text("The game has started!", NamedTextColor.GREEN)));
        startGame();
    }

    private void startGame() {
        botBowsGame = switch (settings.currentMap) {
            case ICY_RAVINE -> new IcyRavineGame(settings);
            case STEAMPUNK -> new SteamPunkGame(settings);
            case SPACE_STATION -> new SpaceStationGame(settings);
            default -> new BotBowsGame(settings);
        };
        botBowsGame.startGame();
        activeGame = true;
    }

    public void reset() {
        activeGame = false;
        new HashSet<>(players.keySet()).forEach(this::leaveGame);
        botBowsGame = null;
        settings = new Settings(this);
        settings.initMenus();
    }

    public void messagePlayers(Component message) {
        for (BotBowsPlayer p : settings.getPlayers()) {
            p.player.sendMessage(message);
        }
    }

    public void titlePlayers(String title, int duration) {
        for (BotBowsPlayer p : players.values()) {
            p.player.sendTitle(title, null, 2, duration, 5);
        }
    }

    public void check4Elimination(BotBowsPlayer dedPlayer) {
        botBowsGame.check4Elimination(dedPlayer);
    }

    public int getTotalPlayers() {
        return players.size();
    }

    public boolean isGameActive() {
        return activeGame;
    }

    public void handlePlayerReady(BotBowsPlayer p) {
        boolean ready = p.isReady();
        long readyPlayers = players.values().stream().filter(BotBowsPlayer::isReady).count();
        int totalPlayers = Math.max(players.size(), 2);

        messagePlayers(Component.text(p.player.getName() +
                (ready ? " has readied up " : " is no longer ready ") +
                "(" + readyPlayers + "/" + totalPlayers + ")", NamedTextColor.YELLOW));
        if (readyPlayers == totalPlayers && !(settings.team1.isEmpty() || settings.team2.isEmpty())) {
            messagePlayers(Component.text("Everybody are ready, starting game in 5 seconds", NamedTextColor.GREEN));
            startGame();
        }
    }
}
