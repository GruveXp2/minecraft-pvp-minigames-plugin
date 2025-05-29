package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.mechanics.SteamPipe;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import org.bukkit.Axis;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SteamPunkGame extends BotBowsGame {

    private final HashSet<SteamPipe> steamPipes = new HashSet<>();
    private final HashMap<Chunk, Set<SteamPipe>> pipeChunks = new HashMap<>();
    private SteamPipeMotor steamPipeMotor; // responsible for powering the pipes by giving them 20 ticks/s

    public SteamPunkGame(Settings settings) {
        super(settings);
        World world = Main.WORLD;

        registerSteamPipe(new SteamPipe(true, List.of(
                new Location(world, -346.5, 17, -360.2),
                new Location(world, -346.5, 17, -353.5),
                new Location(world, -365.2, 17, -353.5)
        ), Axis.Z, Axis.X));
    }

    private void registerSteamPipe(SteamPipe steamPipe) {
        steamPipes.add(steamPipe);
        Set<Chunk> chunks = steamPipe.getTickedChunks();
        for (Chunk chunk : chunks) {
            pipeChunks.computeIfAbsent(chunk, k -> new HashSet<>()).add(steamPipe);
        }
    }

    @Override
    public void startRound() {
        super.startRound();
        steamPipeMotor = new SteamPipeMotor(steamPipes);
        steamPipeMotor.runTaskTimer(Main.getPlugin(), 200L, 1L);
    }

    @Override
    public void handleMovement(PlayerMoveEvent e) {
        super.handleMovement(e);
        Player p = e.getPlayer();
        Chunk chunk = p.getChunk();
        Set<SteamPipe> pipes = pipeChunks.get(chunk);
        if (pipes != null) {
            pipes.forEach(pipe -> pipe.checkProximity(p));
        }
    }

    @Override
    public void postGame(BotBowsTeam winningTeam) {
        super.postGame(winningTeam);
        steamPipeMotor.cancel();
        steamPipeMotor = null;
    }


    private static class SteamPipeMotor extends BukkitRunnable {

        public final HashSet<SteamPipe> steamPipes;

        public SteamPipeMotor(HashSet<SteamPipe> steamPipes) {
            this.steamPipes = steamPipes;
        }

        @Override
        public void run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            steamPipes.forEach(SteamPipe::tick);
        }
    }
}
