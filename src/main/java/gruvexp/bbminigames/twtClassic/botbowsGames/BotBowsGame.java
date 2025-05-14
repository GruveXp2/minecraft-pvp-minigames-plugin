package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.tasks.BotBowsGiver;
import gruvexp.bbminigames.tasks.RoundCountdown;
import gruvexp.bbminigames.tasks.RoundTimer;
import gruvexp.bbminigames.twtClassic.*;
import gruvexp.bbminigames.twtClassic.ability.abilities.CreeperTrapAbility;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Set;

import gruvexp.bbminigames.twtClassic.BarManager;
import org.bukkit.scheduler.BukkitTask;

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
    public boolean canShoot = false;
    public boolean activeRound = false;
    protected int round = 0; // hvilken runde man er på
    private BukkitTask roundTimer;

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

    public void startGame() {
        barManager.sneakBarInit();
        Cooldowns.CoolDownInit(players);
        boardManager.createBoard();
        startRound();
        stormHazard.init();
        earthquakeHazard.init();

        // legger til player liv osv
        for (BotBowsPlayer q : players) {
            q.initBattle();
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
            p.readyAbilities();
        }
        // teleporterer til spawn
        team1.tpPlayersToSpawn();
        team2.tpPlayersToSpawn();
        canMove = false;
        canShoot = false;
        activeRound = true;
        new RoundCountdown(this, round).runTaskTimer(Main.getPlugin(), 0L, 20L); // mens de er på spawn, kan de ikke bevege seg og det er nedtelling til det begynner
        if (settings.getRoundDuration() != 0) {
            roundTimer = new RoundTimer(this, settings.getRoundDuration()).runTaskTimer(Main.getPlugin(), 200L, 20L);
        }
    }

    public void triggerHazards() {
        stormHazard.triggerOnChance();
        earthquakeHazard.triggerOnChance();
        ghostHazard.triggerOnChance();
    }

    public void handleMovement(PlayerMoveEvent e) {
        BotBows.handleMovement(e);
    }

    public void check4Elimination(BotBowsPlayer dedPlayer) {
        BotBowsTeam losingTeam = dedPlayer.getTeam();

        if (losingTeam.isEliminated()) {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> endGameEliminated(losingTeam), 2L);
        }
    }

    private void endGameEliminated(BotBowsTeam losingTeam) {
        BotBowsTeam winningTeam = losingTeam.getOppositeTeam();
        if (winningTeam.isEliminated()) { // begge daua på likt
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW));
            postRound(null, 0);
            return;
        }
        lobby.messagePlayers(winningTeam.toComponent()
                .append(Component.text(" won the round!", NamedTextColor.GREEN)));
        int winScore = settings.dynamicScoringEnabled() ? calculateDynamicScore(winningTeam, losingTeam) : 1;
        winningTeam.addPoints(winScore);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> postRound(winningTeam, winScore), 2L); // 2 ticks delay i tilfelle alle dauer rett etterpå, da skal det bli draw isteden
    }

    public void endGameTimeout() {
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
            int winScore = settings.dynamicScoringEnabled() ? calculateDynamicScore(winningTeam, losingTeam) : 1;
            winningTeam.addPoints(winScore);
            postRound(winningTeam, winScore);
        } else {
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW));
            postRound(null, 0);
        }
    }

    private void postRound(BotBowsTeam winningTeam, int winScore) {
        if (!activeRound) return;
        activeRound = false;
        CreeperTrapAbility.igniteAllCreepers();
        lobby.messagePlayers( // team1: %d points, team2: %d points
                team1.toComponent()
                        .append(Component.text(": ", NamedTextColor.WHITE))
                        .append(Component.text(team1.getPoints() + "\n", NamedTextColor.GREEN))
                        .append(team2.toComponent())
                        .append(Component.text(": ", NamedTextColor.WHITE))
                        .append(Component.text(team2.getPoints(), NamedTextColor.GREEN)));
        if (settings.getRoundDuration() > 0) {
            roundTimer.cancel();
        }
        if (settings.rain > 0) {
            Main.WORLD.setStorm(false);
            Bukkit.getOnlinePlayers().forEach(Player::resetPlayerWeather);
        }
        if (stormHazard.isActive()) stormHazard.end();
        if (earthquakeHazard.isActive()) earthquakeHazard.end();
        if (ghostHazard.isActive()) ghostHazard.end();

        if (winningTeam == null) {
            lobby.titlePlayers(ChatColor.YELLOW + "DRAW", 40);
            canShoot = false;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::startRound, 40L);
            return;
        }

        lobby.titlePlayers(BoardManager.toChatColor((NamedTextColor) winningTeam.color) + winningTeam.name + " +" + winScore, 40);
        boardManager.updateTeamScores();

        if (winningTeam.getPoints() >= settings.getWinScoreThreshold() && settings.getWinScoreThreshold() > 0) {
            postGame(winningTeam);
        } else {
            canShoot = false;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::startRound, 40L);
        }
    }

    private int calculateDynamicScore(BotBowsTeam winningTeam, BotBowsTeam losingTeam) {
        int HPLeft = 0;
        for (BotBowsPlayer p : winningTeam.getPlayers()) {
            HPLeft += p.getHP();
        }
        lobby.messagePlayers(Component.text(HPLeft + "p for remaining hp", winningTeam.color));

        int enemyHPTaken = 0;
        for (BotBowsPlayer p : losingTeam.getPlayers()) {
            enemyHPTaken += p.getMaxHP();
        }
        lobby.messagePlayers(Component.text(enemyHPTaken + "p for enemy hp lost", winningTeam.color));

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
                    "TEAM " + winningTeam.name.toUpperCase() + " won the game after " + round + " round" + (round == 1 ? "" : "s") + "! GG\n" +
                    "================", winningTeam.color));
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
        lobby.reset();
    }

    private void postGameTitle(BotBowsTeam winningTeam) {
        if (winningTeam == null) {
            return;
        }
        BotBowsTeam losingTeam = winningTeam.getOppositeTeam();
        for (BotBowsPlayer p : winningTeam.getPlayers()) {
            p.player.showTitle(Title.title(Component.text("Victory", winningTeam.color), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        }
        for (BotBowsPlayer p : losingTeam.getPlayers()) {
            p.player.showTitle(Title.title(Component.text("Defeat", losingTeam.color), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))));
        }
    }

    public void endGame() { // the game has ended, check who won
        if (settings.getRoundDuration() > 0) {
            roundTimer.cancel();
        }
        if (stormHazard.isActive()) stormHazard.end();
        if (earthquakeHazard.isActive()) earthquakeHazard.end();
        if (ghostHazard.isActive()) ghostHazard.end();

        if (team1.getPoints() == team2.getPoints()) {
            postGame(null);
        } else if (team1.getPoints() > team2.getPoints()) {
            postGame(team1);
        } else {
            postGame(team2);
        }
    }
}