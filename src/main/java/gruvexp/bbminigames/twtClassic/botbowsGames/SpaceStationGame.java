package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.mechanics.SpaceStationDoor;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpaceStationGame extends BotBowsGame {

    private final Set<SpaceStationDoor> doors = new HashSet<>();
    private final Map<SpaceStationDoor, DoorMotor> doorMotors = new HashMap<>();

    public SpaceStationGame(Settings settings) {
        super(settings);
        World world = Main.WORLD_END;

        doors.add(new SpaceStationDoor(new Location(world, 147, 74, 188), Axis.Z)); // lower green
        doors.add(new SpaceStationDoor(new Location(world, 147, 86, 180), Axis.Z)); // upper green
        doors.add(new SpaceStationDoor(new Location(world, 140, 86, 171), Axis.X)); // green-orange
        doors.add(new SpaceStationDoor(new Location(world, 136, 74, 199), Axis.X)); // lower orange
        doors.add(new SpaceStationDoor(new Location(world, 138, 86, 199), Axis.X)); // upper orange
        doors.add(new SpaceStationDoor(new Location(world, 147, 86, 212), Axis.Z)); // upper blue
        doors.add(new SpaceStationDoor(new Location(world, 134, 74, 242), Axis.X)); // blue-red
        doors.add(new SpaceStationDoor(new Location(world, 162, 74, 199), Axis.X)); // lower red
        doors.add(new SpaceStationDoor(new Location(world, 160, 86, 199), Axis.X)); // upper red
    }

    @Override
    public void startRound() {
        super.startRound();
        doors.forEach(door -> {
            door.open();
            doorMotors.put(door, new DoorMotor(door));
            scheduleDoor(door);
        });
    }

    @Override
    protected void postRound(BotBowsTeam winningTeam, int winScore) {
        doorMotors.values().forEach(BukkitRunnable::cancel);
        doorMotors.clear();
        doors.forEach(SpaceStationDoor::open);
        super.postRound(winningTeam, winScore);
    }

    private void scheduleDoor(SpaceStationDoor door) {
        int randomDelay = BotBows.RANDOM.nextInt(5) + 5 + ((int) Main.WORLD_END.getFullTime() % 300 / 10); // they toggle each 5-10 - 35-40 seconds
        if (door.isOpen()) { // the game switches between times when the doors are open ≈85% of the time and ≈15% of the time
            randomDelay = 45 - randomDelay;
        }
        DoorMotor motor = new DoorMotor(door);
        motor.runTaskLater(Main.getPlugin(), randomDelay * 20);
        doorMotors.put(door, motor);
    }


    private class DoorMotor extends BukkitRunnable {

        public final SpaceStationDoor door;

        public DoorMotor(SpaceStationDoor door) {
            this.door = door;
        }

        @Override
        public void run() {
            door.toggle();
            scheduleDoor(door);
        }
    }
}
