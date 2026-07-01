package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.GvwDungeonProximityScanner;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.DungeonGhoster;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IcyRavineGame extends BotBowsGame {

    private static final Map<BotBowsPlayer, GvwDungeonProximityScanner> dungeonScanners = new HashMap<>();
    private static final Map<BotBowsPlayer, DungeonGhoster> dungeonGhosters = new HashMap<>();

    public IcyRavineGame(Settings settings) {
        super(settings);
    }

    @Override
    public void leaveGame(BotBowsPlayer bp) {
        super.leaveGame(bp);
        if (dungeonScanners.containsKey(bp)) {
            dungeonScanners.get(bp).cancel();
            dungeonScanners.remove(bp);
            dungeonGhosters.remove(bp);
        }
    }

    @Override
    public void startGame() {
        super.startGame();
        initDungeon();
    }

    @Override
    public void startRound() {
        super.startRound();
        startScanners();
    }

    @Override
    public void handleMovement(PlayerMoveEvent e) {
        super.handleMovement(e);
        UUID playerId = e.getPlayer().getUniqueId();
        BotBowsPlayer bp = lobby.getBotBowsPlayer(playerId);
        if (settings.isPlayerJoined(playerId) && isInDungeon(bp)) {
            handleDungeonMovement(bp);
        }
    }

    @Override
    public void postGame(BotBowsTeam winningTeam) {
        super.postGame(winningTeam);
        for (BukkitRunnable scanner : dungeonScanners.values()) {
            scanner.cancel();
        }
        dungeonScanners.clear();
        dungeonGhosters.clear();
    }

    private void initDungeon() {
        for (BotBowsPlayer bp : players) {
            dungeonGhosters.put(bp, new DungeonGhoster(bp));
        }
    }

    private void startScanners() {
        for (BotBowsPlayer bp : players) {
            GvwDungeonProximityScanner scanner = new GvwDungeonProximityScanner(bp);
            dungeonScanners.put(bp, scanner);
            scanner.runTaskTimer(Main.getPlugin(), 140L, 5L);
        }
        //debugMessage("starting scanners");
    }

    public boolean isInDungeon(BotBowsPlayer bp) {
        return dungeonScanners.get(bp).isInDungeon();
    }

    public String getSection(BotBowsPlayer bp) {
        return dungeonGhosters.get(bp).getSection();
    }

    public void handleDungeonMovement(BotBowsPlayer bp) {
        dungeonGhosters.get(bp).handleMovement();
    }
}
