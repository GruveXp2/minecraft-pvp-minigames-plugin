package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.mechanics.*;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam;
import org.bukkit.Axis;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3i;

import java.util.*;

public class SteamPunkGame extends BotBowsGame {

    public static final int DOOR_TOGGLE_DELAY = 15 * 20;

    private final Set<SteamPipe> steamPipes = new HashSet<>();
    private final Map<Chunk, Set<SteamPipe>> pipeChunks = new HashMap<>();
    private SteamPipeMotor steamPipeMotor; // responsible for powering the pipes by giving them 20 ticks/s

    private final Set<Hatch> hatches = new HashSet<>();
    private final Map<Hatch, HatchMotor> hatchMotors = new HashMap<>();

    private final Set<Impeller> impellers = new HashSet<>();
    private final Map<Chunk, Set<Impeller>> impellerChunks = new HashMap<>();
    private ImpellerMotor impellerMotor; // responsible for powering the pipes by giving them 20 ticks/s

    private final Set<Gate> gates = new HashSet<>();
    private GateMotor gateMotor;

    private final Set<Gear> bigWheels = new HashSet<>();

    private final Set<Rotor> rotors = new HashSet<>(); // spinning blades that hold up the upper parts of the arena

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
        // exposed
        hatches.add(new Hatch(1, new Location(world, -357, 21, -395), StructureRotation.COUNTERCLOCKWISE_90, "copper_hatch_exposed")); // copper
        hatches.add(new Hatch(2, new Location(world, -358, 21, -395), StructureRotation.CLOCKWISE_90, "copper_hatch_exposed")); // weathered
        // weathered
        hatches.add(new Hatch(1, new Location(world, -357, 21, -358), StructureRotation.COUNTERCLOCKWISE_90, "copper_hatch_weathered")); // exposed
        hatches.add(new Hatch(2, new Location(world, -358, 21, -358), StructureRotation.CLOCKWISE_90, "copper_hatch_weathered")); // oxidized

        // impellers
        registerImpeller(new Impeller(1, "copper_impeller_oxidized",
                new Location(world, -371, 16, -381),
                2));
        registerImpeller(new Impeller(1, "copper_impeller_exposed", // center
                new Location(world, -371, 16, -387),
                -4));
        registerImpeller(new Impeller(2, "copper_impeller_exposed", // next to pipe
                new Location(world, -371, 16, -392),
                4));

        // gates
        Vector3i gateSize = new Vector3i(3, 7, 7);
        Location gateFramesSrc = new Location(world, -400, 1, -327);
        // copper
        gates.add(new Gate(gateFramesSrc, 3, gateSize, new Location(world, -347, 22, -397), 2, true,
                Set.of(
                        new Gear(1, new Location(world, -348, 26, -399), StructureRotation.NONE, "copper_wheel", 25),
                        new Gear(2, new Location(world, -344, 26, -399), StructureRotation.NONE, "copper_wheel", 25),
                        new Gear(3, new Location(world, -348, 24, -389), StructureRotation.NONE, "copper_wheel", -12)
                )
        ));

        // exposed
        gates.add(new Gate(gateFramesSrc.clone().add(12, 0, 0), 3, gateSize, new Location(world, -347, 22, -362), 3, false,
                Set.of(
                        new Gear(1, new Location(world, -348, 26, -354), StructureRotation.NONE, "copper_wheel_exposed", 16),
                        new Gear(2, new Location(world, -344, 26, -354), StructureRotation.NONE, "copper_wheel_exposed", 16),
                        new Gear(3, new Location(world, -348, 24, -364), StructureRotation.NONE, "copper_wheel_exposed", -8)
                )
        ));

        // weathered
        gates.add(new Gate(gateFramesSrc.clone().add(24, 0, 0), 3, gateSize, new Location(world, -370, 22, -397), 4, false,
                Set.of(
                        new Gear(1, new Location(world, -367, 26, -399), StructureRotation.NONE, "copper_wheel_weathered", 9),
                        new Gear(2, new Location(world, -371, 26, -399), StructureRotation.NONE, "copper_wheel_weathered", 9),
                        new Gear(3, new Location(world, -367, 24, -389), StructureRotation.NONE, "copper_wheel_weathered", -4.5f)
                )
        ));

        // oxidized
        gates.add(new Gate(gateFramesSrc.clone().add(36, 0, 0), 3, gateSize, new Location(world, -370, 22, -362), 6, true,
                Set.of(
                        new Gear(1, new Location(world, -367, 26, -354), StructureRotation.NONE, "copper_wheel_oxidized", 5),
                        new Gear(2, new Location(world, -371, 26, -354), StructureRotation.NONE, "copper_wheel_oxidized", 5),
                        new Gear(3, new Location(world, -367, 24, -364), StructureRotation.NONE, "copper_wheel_oxidized", -2.5f)
                )
        ));

        bigWheels.add(new Gear(1, new Location(world, -339, 21, -396), StructureRotation.NONE, "big_copper_wheel", 8, 5));
        bigWheels.add(new Gear(1, new Location(world, -339, 21, -357), StructureRotation.NONE, "big_copper_wheel_exposed", -5, 5));
        bigWheels.add(new Gear(1, new Location(world, -376, 21, -396), StructureRotation.NONE, "big_copper_wheel_weathered", 3, 5));
        bigWheels.add(new Gear(1, new Location(world, -376, 21, -357), StructureRotation.NONE, "big_copper_wheel_oxidized", -2, 5));

        rotors.add(new Rotor(1, new Location(world, -332, 39, -391), "copper_rotor", 50, 4));
        rotors.add(new Rotor(2, new Location(world, -336, 39, -391), "copper_rotor", 50, 4));
        rotors.add(new Rotor(3, new Location(world, -344, 39, -395), "copper_rotor", 50, 4));
        rotors.add(new Rotor(4, new Location(world, -344, 39, -392), "copper_rotor", 50, 4));
        rotors.add(new Rotor(5, new Location(world, -343, 39, -381), "copper_rotor", 50, 4));
        rotors.add(new Rotor(6, new Location(world, -343, 39, -372), "copper_rotor", 50, 4));
        rotors.add(new Rotor(1, new Location(world, -332, 39, -362), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(2, new Location(world, -336, 39, -362), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(3, new Location(world, -344, 39, -358), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(4, new Location(world, -344, 39, -361), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(5, new Location(world, -351, 39, -389), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(6, new Location(world, -358, 39, -391), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(7, new Location(world, -358, 39, -394), "copper_rotor_exposed", 37, 5));
        rotors.add(new Rotor(1, new Location(world, -351, 39, -364), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(2, new Location(world, -357, 39, -359), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(3, new Location(world, -357, 39, -362), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(4, new Location(world, -364, 39, -389), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(5, new Location(world, -371, 39, -395), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(6, new Location(world, -371, 39, -392), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(7, new Location(world, -372, 39, -381), "copper_rotor_weathered", 25, 7));
        rotors.add(new Rotor(7, new Location(world, -363, 39, -364), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -371, 39, -358), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -371, 39, -361), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -372, 39, -372), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -379, 39, -391), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -383, 39, -391), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -383, 39, -362), "copper_rotor_oxidized", 18, 9));
        rotors.add(new Rotor(7, new Location(world, -379, 39, -362), "copper_rotor_oxidized", 18, 9));
    }

    private void registerSteamPipe(SteamPipe steamPipe) {
        steamPipes.add(steamPipe);
        Set<Chunk> chunks = steamPipe.getTickedChunks();
        for (Chunk chunk : chunks) {
            pipeChunks.computeIfAbsent(chunk, _ -> new HashSet<>()).add(steamPipe);
        }
    }

    private void registerImpeller(Impeller impeller) {
        impellers.add(impeller);
        Set<Chunk> chunks = impeller.getTickedChunks();
        for (Chunk chunk : chunks) {
            impellerChunks.computeIfAbsent(chunk, _ -> new HashSet<>()).add(impeller);
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
        impellerMotor = new ImpellerMotor(impellers);
        impellerMotor.runTaskTimer(plugin, 200, 1);
        gateMotor = new GateMotor(gates);
        gateMotor.runTaskTimer(plugin, 200, DOOR_TOGGLE_DELAY);

        bigWheels.forEach(wheel -> wheel.rotate(360 * 1225)); // 1225 POINTs
        rotors.forEach(Rotor::startRotating);
    }

    @Override
    protected void postRound(BotBowsTeam winningTeam, int winScore) {
        stopMotors();
        super.postRound(winningTeam, winScore);
    }

    @Override
    public void postGame(BotBowsTeam winningTeam) {
        if (steamPipeMotor != null) stopMotors(); // stop motors unless they already got stopped in postRound()
        bigWheels.forEach(Gear::stop);
        rotors.forEach(Rotor::stop);
        super.postGame(winningTeam);
    }

    private void stopMotors() {
        steamPipeMotor.cancel();
        steamPipeMotor = null;
        hatchMotors.values().forEach(BukkitRunnable::cancel);
        hatchMotors.clear();
        impellerMotor.cancel();
        impellerMotor = null;
        gateMotor.cancel();
        gateMotor = null;
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

        Set<Impeller> impellers = impellerChunks.get(chunk);
        if (impellers != null) {
            impellers.forEach(impeller -> impeller.checkProximity(p));
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

    private static class ImpellerMotor extends BukkitRunnable {

        public final Set<Impeller> impellers;

        public ImpellerMotor(Set<Impeller> impellers) {
            this.impellers = impellers;
        }

        @Override
        public void run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            impellers.forEach(Impeller::tick);
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
