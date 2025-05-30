package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.mechanics.Hatch;
import gruvexp.bbminigames.mechanics.SteamPipe;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import org.bukkit.Axis;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SteamPunkGame extends BotBowsGame {

    private final HashSet<SteamPipe> steamPipes = new HashSet<>();
    private final HashMap<Chunk, Set<SteamPipe>> pipeChunks = new HashMap<>();
    private SteamPipeMotor steamPipeMotor; // responsible for powering the pipes by giving them 20 ticks/s

    private final Set<Hatch> hatches = new HashSet<>();
    private final Map<Hatch, HatchMotor> hatchMotors = new HashMap<>();

    public SteamPunkGame(Settings settings) {
        super(settings);
        World world = Main.WORLD;
        // steam pipes
        // copper -> weathered
        registerSteamPipe(new SteamPipe(true, List.of(
                new Location(world, -350.8, 17, -398.5),
                new Location(world, -371.5, 17, -398.5),
                new Location(world, -371.5, 17, -395.8)
        ), Axis.X, Axis.Z));
        // copper -> surface
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -351.2, 17, -394.5),
                new Location(world, -350.5, 17, -394.5),
                new Location(world, -350.5, 17.9, -387.5),
                new Location(world, -350.5, 25, -387.8),
                new Location(world, -350.5, 25, -384.5),
                new Location(world, -357.5, 25.5, -384.5),
                new Location(world, -357.2, 26.9, -384.5)
        ), Axis.X, Axis.Y));

        // exposed -> exposed
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -352.2,  18,  -368.5),
                new Location(world, -340.5, 18, -368.5)
        ), Axis.X, Axis.X));
        // exposed -> weathered
        registerSteamPipe(new SteamPipe(true, List.of(
                new Location(world, -352.2,  18,  -370.5),
                new Location(world, -350.5, 18, -370.5),
                new Location(world, -350.5, 18, -379.5),
                new Location(world, -361.2, 18, -379.5)
        ), Axis.X, Axis.X));
        // exposed -> oxidized
        registerSteamPipe(new SteamPipe(true, List.of(
                new Location(world, -346.5, 17.5, -360.2),
                new Location(world, -346.5, 17, -353.5),
                new Location(world, -365.2, 17.5, -353.5)
        ), Axis.Z, Axis.X));
        // exposed -> surface
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -351.2, 17, -357.5),
                new Location(world, -350.5, 17, -357.5),
                new Location(world, -350.5, 17.9, -364.5),
                new Location(world, -350.5, 25.5, -364.2),
                new Location(world, -350.5, 25, -367.5),
                new Location(world, -356.5, 25.5, -367.5),
                new Location(world, -356.2, 26.9, -367.5)
        ), Axis.X, Axis.Y));

        // surface_copper -> gate_left
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -333.8, 25, -369.5),
                new Location(world, -338.3, 25, -369.5),
                new Location(world, -338.2, 25, -367.5),
                new Location(world, -342.5, 25, -367.5),
                new Location(world, -343.5, 25, -361.5),
                new Location(world, -342.5, 31.9, -361.2),
                new Location(world, -342.5, 31, -358.5),
                new Location(world, -347.5, 31, -358.5)
        ), Axis.Z, Axis.X));
        // surface_copper -> gate_right
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -333.8, 25, -382.5),
                new Location(world, -338.3, 25, -382.5),
                new Location(world, -338.2, 25, -384.5),
                new Location(world, -342.5, 25, -384.5),
                new Location(world, -343.5, 25, -390.5),
                new Location(world, -342.5, 31.9, -390.8),
                new Location(world, -342.5, 31, -393.5),
                new Location(world, -347.5, 31, -393.5)
        ), Axis.Z, Axis.X));

        // surface_oxidized -> gate_left
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -380.2, 25, -369.5),
                new Location(world, -375.7, 25, -369.5),
                new Location(world, -375.8, 25, -367.5),
                new Location(world, -371.5, 25, -367.5),
                new Location(world, -371.5, 25, -361.5),
                new Location(world, -371.5, 31.9, -361.2),
                new Location(world, -371.5, 31, -358.5),
                new Location(world, -366.5, 31, -358.5)
        ), Axis.Z, Axis.X));
        // surface_oxidized -> gate_right
        registerSteamPipe(new SteamPipe(false, List.of(
                new Location(world, -380.2, 25, -382.5),
                new Location(world, -375.7, 25, -382.5),
                new Location(world, -375.8, 25, -384.5),
                new Location(world, -371.5, 25, -384.5),
                new Location(world, -371.5, 25, -390.5),
                new Location(world, -371.5, 31.9, -390.8),
                new Location(world, -371.5, 31, -393.5),
                new Location(world, -366.5, 31, -393.5)
        ), Axis.Z, Axis.X));

        //hatches
        //copper
        hatches.add(new Hatch("steampunk_hatch_copper",
                new Vector(-356, 21, -396),
                new Vector(4, 3, 0),
                new Vector(-357, 22, -396),
                new Vector(0, 4, 3)));
        //weathered
        hatches.add(new Hatch("steampunk_hatch_weathered",
                new Vector(-362, 21, -396),
                new Vector(4, 3, 0),
                new Vector(-358, 22, -396),
                new Vector(0, 4, 3)));
        //exposed
        hatches.add(new Hatch("steampunk_hatch_exposed",
                new Vector(-356, 21, -359),
                new Vector(4, 3, 0),
                new Vector(-357, 22, -359),
                new Vector(0, 4, 3)));
        //oxidized
        hatches.add(new Hatch("steampunk_hatch_oxidized",
                new Vector(-362, 21, -359),
                new Vector(4, 3, 0),
                new Vector(-358, 22, -359),
                new Vector(0, 4, 3)));
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
        hatches.forEach(hatch -> {
            hatchMotors.put(hatch, new HatchMotor(hatch));
            scheduleHatch(hatch);
        });
    }

    @Override
    protected void postRound(BotBowsTeam winningTeam, int winScore) {
        steamPipeMotor.cancel();
        steamPipeMotor = null;
        hatchMotors.values().forEach(BukkitRunnable::cancel);
        hatchMotors.clear();
        super.postRound(winningTeam, winScore);
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

    private void scheduleHatch(Hatch hatch) {
        int randomDelay = BotBows.RANDOM.nextInt(14) + 2; // they toggle each 2-16 seconds
        HatchMotor motor = new HatchMotor(hatch);
        motor.runTaskLater(Main.getPlugin(), randomDelay * 20);
        hatchMotors.put(hatch, motor);
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

    private class HatchMotor extends BukkitRunnable {

        public final Hatch hatch;

        public HatchMotor(Hatch hatch) {
            this.hatch = hatch;
        }

        @Override
        public void run() {
            hatch.toggle();
            scheduleHatch(hatch);
        }
    }
}
