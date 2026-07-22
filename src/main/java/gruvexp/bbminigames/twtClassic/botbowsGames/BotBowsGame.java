package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.BotBowsGiver;
import gruvexp.bbminigames.tasks.RoundCountdown;
import gruvexp.bbminigames.tasks.RoundTimer;
import gruvexp.bbminigames.twtClassic.*;
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import gruvexp.bbminigames.twtClassic.settings.WinConditionSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.scheduler.BukkitTask;

public class BotBowsGame {

    public final Settings settings;
    public final Lobby lobby;
    protected final BotBowsTeam team1;
    protected final BotBowsTeam team2;
    protected final Set<BotBowsPlayer> players;
    public final BoardManager boardManager;
    protected final Collection<Hazard> hazards;

    public boolean canMove = true;
    public boolean canInteract = false; // if you are able to shoot or use abilities
    public boolean activeRound = false; // if the game is currently ongoing, this includes the countdown in the start of rounds
    protected int round = 0; // hvilken runde man er på
    private BukkitTask roundTimer;

    public BotBowsGame(Settings settings) {
        this.settings = settings;
        this.lobby = settings.lobby;
        this.team1 = settings.team1;
        this.team2 = settings.team2;
        this.players = settings.getPlayers();
        this.hazards = settings.getHazardSettings().createActiveHazards();
        this.boardManager = new BoardManager(lobby);
    }

    public void leaveGame(BotBowsPlayer bp) {
        BotBowsTeam team = bp.getTeam();
        settings.leaveGame(bp);
        boardManager.removePlayerScore(bp);
        if (team.isEmpty()) Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> endGame(), 10L);
    }

    public void startGame() {
        boardManager.createBoard();
        startRound();
        hazards.forEach(hazard -> hazard.init(players));

        // legger til player liv osv
        for (BotBowsPlayer bp : players) {
            bp.initBattle(boardManager.getTeamManager());
            boardManager.updatePlayerScore(bp);
        }
        boardManager.initPlayers(); // makes the player join the Team's to get the correct color outline
        boardManager.updateTeamScores();
        players.forEach(BotBowsPlayer::start);
        new BotBowsGiver(lobby).runTaskTimer(Main.getPlugin(), 100L, 10L);
    }
    public void startRound() {
        round ++;
        // alle har fullt med liv
        for (BotBowsPlayer bp : players) {
            bp.revive();
            bp.readyAbilities();
        }
        // teleporterer til spawn
        team1.tpPlayersToSpawn();
        team2.tpPlayersToSpawn();
        canMove = false;
        canInteract = false;
        activeRound = true;
        new RoundCountdown(this, round).runTaskTimer(Main.getPlugin(), 0L, 20L); // mens de er på spawn, kan de ikke bevege seg og det er nedtelling til det begynner
        int roundDuration = settings.getWinConditionSettings().getRoundDuration();
        if (roundDuration != 0) {
            roundTimer = new RoundTimer(this, roundDuration).runTaskTimer(Main.getPlugin(), 200L, 20L);
        }
    }

    public void triggerHazards() {
        hazards.forEach(hazard -> hazard.triggerOnChance(players));
    }

    public Hazard getStormHazard() { // temporary until trident ability is revamped
        List<Hazard> stormHazard = hazards.stream().filter(hazard -> hazard instanceof StormHazard).toList();
        if (!stormHazard.isEmpty()) return stormHazard.getFirst();
        return null;
    }

    public void handleMovement(PlayerMoveEvent e) {
        BotBows.handleMovement(e);
    }

    public void check4Elimination(BotBowsPlayer dedPlayer) {
        BotBowsTeam losingTeam = dedPlayer.getTeam();

        if (losingTeam.isEliminated()) {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> endRoundEliminated(losingTeam), 2L);
        }
    }

    private void endRoundEliminated(BotBowsTeam losingTeam) {
        BotBowsTeam winningTeam = losingTeam.getOppositeTeam();
        if (winningTeam.isEliminated()) { // begge daua på likt
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW));
            postRound(null, 0);
            return;
        }
        lobby.messagePlayers(winningTeam.toComponent()
                .append(Component.text(" won the round!", NamedTextColor.GREEN)));
        int winScore = settings.getWinConditionSettings().isDynamicScoring() ? calculateDynamicScore(winningTeam, losingTeam) : 1;
        winningTeam.addPoints(winScore);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> postRound(winningTeam, winScore), 2L); // 2 ticks delay i tilfelle alle dauer rett etterpå, da skal det bli draw isteden
    }

    public void endRoundTimeout() {
        int team1Percentage = team1.getHealthPercentage();
        int team2Percentage = team2.getHealthPercentage();
        BotBowsTeam winningTeam = null;
        NamedTextColor team1ResultColor;
        NamedTextColor team2ResultColor;
        if (team1Percentage > team2Percentage) {
            winningTeam = team1;
            team1ResultColor = NamedTextColor.GREEN;
            team2ResultColor = NamedTextColor.RED;
        } else if (team1Percentage < team2Percentage) {
            winningTeam = team2;
            team1ResultColor = NamedTextColor.RED;
            team2ResultColor = NamedTextColor.GREEN;
        } else {
            team1ResultColor = team2ResultColor = NamedTextColor.YELLOW;
        }
        lobby.messagePlayers(Component.empty()
                .append(Component.text("Round over!\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(team1.toComponent())
                .append(Component.text(": "))
                .append(Component.text(team1Percentage, team1ResultColor))
                .append(Component.text("% hp left\n", team1ResultColor))
                .append(team2.toComponent())
                .append(Component.text(": "))
                .append(Component.text(team2Percentage, team2ResultColor))
                .append(Component.text("% hp left\n", team2ResultColor)));
        if (winningTeam != null) {
            lobby.messagePlayers(winningTeam.toComponent()
                    .append(Component.text(" won the round!", NamedTextColor.GREEN)));
            BotBowsTeam losingTeam = winningTeam.getOppositeTeam();
            int winScore = settings.getWinConditionSettings().isDynamicScoring() ? calculateDynamicScore(winningTeam, losingTeam) : 1;
            winningTeam.addPoints(winScore);
            postRound(winningTeam, winScore);
        } else {
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW));
            postRound(null, 0);
        }
    }

    protected void postRound(BotBowsTeam winningTeam, int winScore) {
        if (!activeRound) return;
        activeRound = false;
        players.forEach(BotBowsPlayer::resetAbilities);
        lobby.messagePlayers( // team1: %d points, team2: %d points
                team1.toComponent()
                        .append(Component.text(": ", NamedTextColor.WHITE))
                        .append(Component.text(team1.getPoints() + "\n", NamedTextColor.GREEN))
                        .append(team2.toComponent())
                        .append(Component.text(": ", NamedTextColor.WHITE))
                        .append(Component.text(team2.getPoints(), NamedTextColor.GREEN)));
        if (settings.getWinConditionSettings().getRoundDuration() > 0) {
            roundTimer.cancel();
        }
        if (settings.rain > 0) {
            Main.WORLD.setStorm(false);
            Bukkit.getOnlinePlayers().forEach(Player::resetPlayerWeather);
        }
        hazards.stream()
                .filter(Hazard::isActive)
                .forEach(Hazard::end);

        if (winningTeam == null) {
            lobby.titlePlayers(Component.text("DRAW", NamedTextColor.YELLOW), 2);
            canInteract = false;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::startRound, 40L);
            return;
        }

        lobby.titlePlayers(Component.text(winningTeam.getDisplayName() + " +" + winScore, winningTeam.getColor()), 2);
        boardManager.updateTeamScores();

        WinConditionSettings winConditionSettings = settings.getWinConditionSettings();
        if (winningTeam.getPoints() >= winConditionSettings.getWinScoreThreshold() && winConditionSettings.getWinScoreThreshold() > 0) {
            postGame(winningTeam);
        } else {
            canInteract = false;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::startRound, 40L);
        }
    }

    private int calculateDynamicScore(BotBowsTeam winningTeam, BotBowsTeam losingTeam) {
        int HPLeft = 0;
        for (BotBowsPlayer bp : winningTeam.getPlayers()) {
            HPLeft += bp.getHP();
        }
        lobby.messagePlayers(Component.text(HPLeft + "p for remaining hp", winningTeam.getColor()));

        int enemyHPTaken = 0;
        for (BotBowsPlayer bp : losingTeam.getPlayers()) {
            enemyHPTaken += bp.settings.getMaxHealth();
        }
        lobby.messagePlayers(Component.text(enemyHPTaken + "p for enemy hp lost", winningTeam.getColor()));

        return HPLeft + enemyHPTaken;
    }

    public void postGame(BotBowsTeam winningTeam) {
        canMove = true;
        if (winningTeam == null) {
            lobby.messagePlayers(Component.text("================\n" +
                    "The game ended in a tie after " + round + " round" + (round == 1 ? "" : "s") + "\n" +
                    "================", NamedTextColor.LIGHT_PURPLE));
        } else {
            lobby.messagePlayers(Component.text("================\n" +
                    "TEAM " + winningTeam.getDisplayName().toUpperCase() + " won the game after " + round + " round" + (round == 1 ? "" : "s") + "! GG\n" +
                    "================", winningTeam.getColor()));
        }
        postGameTitle(winningTeam);

        Main.WORLD.setThundering(false);
        Main.WORLD.setStorm(false);
        Main.WORLD.setClearWeatherDuration(10000);

        team1.reset();
        team2.reset();
        lobby.reset();
    }

    private void postGameTitle(BotBowsTeam winningTeam) {
        if (winningTeam == null) {
            return;
        }
        BotBowsTeam losingTeam = winningTeam.getOppositeTeam();
        for (BotBowsPlayer bp : winningTeam.getPlayers()) {
            bp.avatar.showTitle(Title.title(Component.text("Victory", winningTeam.getColor()), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        }
        for (BotBowsPlayer bp : losingTeam.getPlayers()) {
            bp.avatar.showTitle(Title.title(Component.text("Defeat", losingTeam.getColor()), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        }
    }

    public void endGame(BotBowsPlayer ender) {
        lobby.messagePlayers(Component.text("The game was ended by ").append(ender.getName()));
        endGame();
    }

    public void endGame() { // the game has ended, check who won
        if (settings.getWinConditionSettings().getRoundDuration() > 0) {
            roundTimer.cancel();
        }
        hazards.stream()
                .filter(Hazard::isActive)
                .forEach(Hazard::end);

        if (team1.getPoints() == team2.getPoints()) {
            postGame(null);
        } else if (team1.getPoints() > team2.getPoints()) {
            postGame(team1);
        } else {
            postGame(team2);
        }
    }
}