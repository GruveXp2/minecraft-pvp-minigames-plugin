package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.BotBowsGiver;
import gruvexp.bbminigames.tasks.RoundCountdown;
import gruvexp.bbminigames.twtClassic.*;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import gruvexp.bbminigames.twtClassic.BarManager;

public class BotBowsGame {

    public final Settings settings;
    public final Lobby lobby;
    protected final BotBowsTeam team1;
    protected final BotBowsTeam team2;
    protected final Set<BotBowsPlayer> players;
    public final BoardManager boardManager;
    public final BarManager barManager;
    protected final StormHazard stormHazard;
    protected final EarthquakeHazard earthquakeHazard;
    protected final GhostHazard ghostHazard;
    public boolean canMove = true;
    protected int round = 0; // hvilken runde man er på

    public BotBowsGame(Settings settings) {
        this.settings = settings;
        this.lobby = settings.lobby;
        this.team1 = settings.team1;
        this.team2 = settings.team2;
        this.players = settings.getPlayers();
        this.stormHazard = settings.stormHazard;
        this.earthquakeHazard = settings.earthquakeHazard;
        this.ghostHazard = settings.ghostHazard;
        this.boardManager = new BoardManager(lobby);
        this.barManager = new BarManager(lobby);
    }

    public void leaveGame(BotBowsPlayer p) {
        // stuff
        settings.leaveGame(p);
        boardManager.removePlayerScore(p);
        if (barManager.sneakBars.containsKey(p.player)) {
            barManager.sneakBars.get(p.player).setVisible(false);
            barManager.sneakBars.get(p.player).removeAll();
            barManager.sneakBars.remove(p.player);
        }
        Cooldowns.sneakCooldowns.remove(p.player);
        if (Cooldowns.sneakRunnables.containsKey(p.player)) {
            Cooldowns.sneakRunnables.get(p.player).cancel();
            Cooldowns.sneakRunnables.remove(p.player);
        }
    }

    public void startGame(Player gameStarter) {
        lobby.messagePlayers(Component.text(gameStarter.getName() + ": ", NamedTextColor.GRAY)
                .append(Component.text("The game has started!", NamedTextColor.GREEN)));
        barManager.sneakBarInit();
        Cooldowns.CoolDownInit(players);
        boardManager.createBoard();
        startRound();
        stormHazard.init();
        earthquakeHazard.init();

        // legger til player liv osv
        for (BotBowsPlayer q : players) {
            q.revive();
            q.readyAbilities();
            boardManager.updatePlayerScore(q);
        }
        boardManager.updateTeamScores();
        new BotBowsGiver(lobby).runTaskTimer(Main.getPlugin(), 100L, 10L);
    }
    public void startRound() {
        round ++;
        // alle har fullt med liv
        for (BotBowsPlayer p : players) {
            p.revive();
        }
        stormHazard.end();
        earthquakeHazard.end();
        // teleporterer til spawn
        team1.tpPlayersToSpawn();
        team2.tpPlayersToSpawn();
        canMove = false;
        new RoundCountdown(this).runTaskTimer(Main.getPlugin(), 0L, 20L); // mens de er på spawn, kan de ikke bevege seg og det er nedtelling til det begynner
    }

    public void triggerHazards() {
        stormHazard.triggerOnChance();
        earthquakeHazard.triggerOnChance();
    }

    public void handleMovement(PlayerMoveEvent e) {
        BotBows.handleMovement(e);
    }

    public void check4Victory(BotBowsPlayer dedPlayer) {
        BotBowsTeam losingTeam = dedPlayer.getTeam();
        BotBowsTeam winningTeam = losingTeam.getOppositeTeam();

        if (!isTeamEliminated(losingTeam)) return;

        lobby.messagePlayers(winningTeam.toComponent()
                .append(Component.text(" won the round!", NamedTextColor.GREEN)));

        int winScore = settings.dynamicScoringEnabled() ? calculateDynamicScore(winningTeam, losingTeam) : 1;
        winningTeam.addPoints(winScore);

        lobby.messagePlayers(team1.toComponent()
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(Component.text(team1.getPoints() + "\n", NamedTextColor.GREEN))
                .append(team2.toComponent())
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(Component.text(team2.getPoints(), NamedTextColor.GREEN)));

        lobby.titlePlayers(BoardManager.toChatColor((NamedTextColor) winningTeam.COLOR) + winningTeam.NAME + " +" + winScore, 40);
        boardManager.updateTeamScores();

        if (winningTeam.getPoints() >= settings.getWinThreshold() && settings.getWinThreshold() > 0) {
            postGame(winningTeam);
        } else {
            startRound();
        }
    }

    private boolean isTeamEliminated(BotBowsTeam team) {
        for (BotBowsPlayer p : team.getPlayers()) {
            if (p.getHP() > 0) {
                return false;
            }
        }
        return true;
    }

    private int calculateDynamicScore(BotBowsTeam winningTeam, BotBowsTeam losingTeam) {
        int HPLeft = 0;
        for (BotBowsPlayer p : winningTeam.getPlayers()) {
            HPLeft += p.getHP();
        }
        lobby.messagePlayers(Component.text(HPLeft + "p for remaining hp", winningTeam.COLOR));

        int enemyHPTaken = 0;
        for (BotBowsPlayer p : losingTeam.getPlayers()) {
            enemyHPTaken += p.getMaxHP();
        }
        lobby.messagePlayers(Component.text(enemyHPTaken + "p for enemy hp lost", winningTeam.COLOR));

        return HPLeft + enemyHPTaken;
    }

    public void postGame(BotBowsTeam winningTeam) {
        lobby.gameEnded();
        canMove = true;
        if (winningTeam == null) {
            lobby.messagePlayers(Component.text("================\n" +
                    "The game ended in a tie after " + round + " round" + (round == 1 ? "" : "s") + "\n" +
                    "================", NamedTextColor.LIGHT_PURPLE));
        } else {
            lobby.messagePlayers(Component.text("================\n" +
                    "TEAM " + winningTeam.NAME.toUpperCase() + " won the game after " + round + " round" + (round == 1 ? "" : "s") + "! GG\n" +
                    "================", winningTeam.COLOR));
        }
        postGameTitle(winningTeam);

        Main.WORLD.setThundering(false);
        Main.WORLD.setStorm(false);
        Main.WORLD.setClearWeatherDuration(10000);

        players.forEach(BotBowsPlayer::reset);
        players.forEach(p -> p.player.getInventory().remove(Material.ARROW));
        boardManager.resetTeams();
        team1.reset();
        team2.reset();
        barManager.sneakBars.clear();
        Cooldowns.sneakCooldowns.clear();
        Cooldowns.sneakRunnables.clear();
        stormHazard.end();
        earthquakeHazard.end();
        settings.abilityMenu.disableAbilities();
    }

    private void postGameTitle(BotBowsTeam winningTeam) {
        if (winningTeam == null) {
            return;
        }
        BotBowsTeam losingTeam = winningTeam.getOppositeTeam();
        for (BotBowsPlayer p : winningTeam.getPlayers()) {
            p.player.showTitle(Title.title(Component.text("Victory", winningTeam.COLOR), Component.text(""),
                    Title.Times.times(Duration.of(500, ChronoUnit.MILLIS), Duration.of(3, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS))));
        }
        for (BotBowsPlayer p : losingTeam.getPlayers()) {
            p.player.showTitle(Title.title(Component.text("Defeat", losingTeam.COLOR), Component.text(""),
                    Title.Times.times(Duration.of(500, ChronoUnit.MILLIS), Duration.of(3, ChronoUnit.SECONDS), Duration.of(1, ChronoUnit.SECONDS))));
        }
    }

    public void endGame() { // the game has ended, check who won
        if (team1.getPoints() == team2.getPoints()) {
            postGame(null);
        } else if (team1.getPoints() > team2.getPoints()) {
            postGame(team1);
        } else {
            postGame(team2);
        }
    }
}