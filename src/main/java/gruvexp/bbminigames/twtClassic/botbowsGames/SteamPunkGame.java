package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.mechanics.Gate;
import gruvexp.bbminigames.mechanics.Hatch;
import gruvexp.bbminigames.mechanics.Spinner;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Vector3i;

import java.util.*;

public class SteamPunkGame extends BotBowsGame {

    private static final int DOOR_TOGGLE_DELAY = 15 * 20;

    private final Set<SteamPipe> steamPipes = new HashSet<>();
    private final Map<Chunk, Set<SteamPipe>> pipeChunks = new HashMap<>();
    private SteamPipeMotor steamPipeMotor; // responsible for powering the pipes by giving them 20 ticks/s

    private final Set<Hatch> hatches = new HashSet<>();
    private final Map<Hatch, HatchMotor> hatchMotors = new HashMap<>();

    private final Set<Spinner> spinners = new HashSet<>();
    private final Map<Chunk, Set<Spinner>> spinnerChunks = new HashMap<>();
    private SpinnerMotor spinnerMotor; // responsible for powering the pipes by giving them 20 ticks/s

    private final Set<Gate> gates = new HashSet<>();
    private GateMotor gateMotor;

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
                new Location(world, -357.5, 25, -384.5),
                new Location(world, -357.5, 26.9, -384.5)
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
                new Location(world, -356.5, 25, -367.5),
                new Location(world, -356.5, 26.9, -367.5)
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

        // hatches
        // copper
        hatches.add(new Hatch("steampunk_hatch_copper",
                new Vector(-356, 21, -396),
                new Vector(4, 1, 3),
                new Vector(-357, 22, -396),
                new Vector(1, 4, 3)));
        // weathered
        hatches.add(new Hatch("steampunk_hatch_weathered",
                new Vector(-362, 21, -396),
                new Vector(4, 1, 3),
                new Vector(-358, 22, -396),
                new Vector(1, 4, 3)));
        // exposed
        hatches.add(new Hatch("steampunk_hatch_exposed",
                new Vector(-356, 21, -359),
                new Vector(4, 1, 3),
                new Vector(-357, 22, -359),
                new Vector(1, 4, 3)));
        // oxidized
        hatches.add(new Hatch("steampunk_hatch_oxidized",
                new Vector(-362, 21, -359),
                new Vector(4, 1, 3),
                new Vector(-358, 22, -359),
                new Vector(1, 4, 3)));

        // spinners
        // oxidized
        registerSpinner(new Spinner("steampunk_spinner_oxidized",
                new Location(world, -370.5, 17, -380.5),
                2));
        // center
        registerSpinner(new Spinner("steampunk_spinner_center",
                new Location(world, -370.5, 17, -386.5),
                -4));
        // pipe
        registerSpinner(new Spinner("steampunk_spinner_pipe",
                new Location(world, -370.5, 17, -391.5),
                4));

        // gates
        Vector3i gateSize = new Vector3i(3, 7, 7);
        Location gateFramesSrc = new Location(world, -400, 1, -327);
        // copper
        gates.add(new Gate(gateFramesSrc                             , 3, gateSize, new Location(world, -347, 22, -397), 2, true));

        // exposed
        gates.add(new Gate(gateFramesSrc.clone().add(12, 0, 0), 3, gateSize, new Location(world, -347, 22, -362), 3, false));

        // weathered
        gates.add(new Gate(gateFramesSrc.clone().add(24, 0, 0), 3, gateSize, new Location(world, -370, 22, -397), 4, false));

        // oxidized
        gates.add(new Gate(gateFramesSrc.clone().add(36, 0, 0), 3, gateSize, new Location(world, -370, 22, -362), 6, true));
    }

    private void registerSteamPipe(SteamPipe steamPipe) {
        steamPipes.add(steamPipe);
        Set<Chunk> chunks = steamPipe.getTickedChunks();
        for (Chunk chunk : chunks) {
            pipeChunks.computeIfAbsent(chunk, k -> new HashSet<>()).add(steamPipe);
        }
    }

    private void registerSpinner(Spinner spinner) {
        spinners.add(spinner);
        Set<Chunk> chunks = spinner.getTickedChunks();
        for (Chunk chunk : chunks) {
            spinnerChunks.computeIfAbsent(chunk, k -> new HashSet<>()).add(spinner);
        }
    }

    @Override
    public void startRound() {
        super.startRound();
        Plugin plugin = Main.getPlugin();
        steamPipeMotor = new SteamPipeMotor(steamPipes);
        steamPipeMotor.runTaskTimer(plugin, 200, 1);
        hatches.forEach(hatch -> {
            hatchMotors.put(hatch, new HatchMotor(hatch));
            scheduleHatch(hatch);
        });
        spinnerMotor = new SpinnerMotor(spinners);
        spinnerMotor.runTaskTimer(plugin, 200, 1);
        gateMotor = new GateMotor(gates);
        gateMotor.runTaskTimer(plugin, 200, DOOR_TOGGLE_DELAY);
    }

    @Override
    protected void postRound(BotBowsTeam winningTeam, int winScore) {
        steamPipeMotor.cancel();
        steamPipeMotor = null;
        hatchMotors.values().forEach(BukkitRunnable::cancel);
        hatchMotors.clear();
        spinnerMotor.cancel();
        spinnerMotor = null;
        gateMotor.cancel();
        gateMotor = null;
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

        Set<Spinner> spinners = spinnerChunks.get(chunk);
        if (spinners != null) {
            spinners.forEach(spinner -> spinner.checkProximity(p));
        }
    }

    private void scheduleHatch(Hatch hatch) {
        int randomDelay = BotBows.RANDOM.nextInt(14) + 2; // they toggle each 2-16 seconds
        HatchMotor motor = new HatchMotor(hatch);
        motor.runTaskLater(Main.getPlugin(), randomDelay * 20);
        hatchMotors.put(hatch, motor);
    }


    private static class SteamPipeMotor extends BukkitRunnable {

        public final Set<SteamPipe> steamPipes;

        public SteamPipeMotor(Set<SteamPipe> steamPipes) {
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

    private static class SpinnerMotor extends BukkitRunnable {

        public final Set<Spinner> spinners;

        public SpinnerMotor(Set<Spinner> spinners) {
            this.spinners = spinners;
        }

        @Override
        public void run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            spinners.forEach(Spinner::tick);
        }
    }

    private static class GateMotor extends BukkitRunnable {

        public final Set<Gate> gates;

        public GateMotor(Set<Gate> gates) {
            this.gates = gates;
        }

        @Override
        public void run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            gates.forEach(Gate::toggle);
        }
    }
}
