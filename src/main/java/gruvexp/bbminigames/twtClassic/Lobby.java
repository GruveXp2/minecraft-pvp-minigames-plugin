package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame;
import gruvexp.bbminigames.twtClassic.botbowsGames.GrautWackyGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;

public class Lobby {

    public final int ID;
    private final HashMap<Player, BotBowsPlayer> players = new HashMap<>(); // liste med alle players som er i gamet
    public Settings settings;
    public BotBowsGame botBowsGame;
    private boolean activeGame = false; // hvis spillet har starta, så kan man ikke gjøre ting som /settings

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
        botBowsGame = switch (settings.currentMap) {
            case ICY_RAVINE -> new GrautWackyGame(settings);
            default -> new BotBowsGame(settings);
        };
        botBowsGame.startGame(gameStarter);
        activeGame = true;
    }

    public void gameEnded() {
        activeGame = false;
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

    public void check4Victory(BotBowsPlayer dedPlayer) {
        botBowsGame.check4Victory(dedPlayer);
    }

    public int getTotalPlayers() {
        return players.size();
    }

    public boolean isGameActive() {
        return activeGame;
    }
}
