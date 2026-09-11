package gruvexp.bbminigames.twtClassic.botbowsGames

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.mechanics.SpaceStationDoor
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class SpaceStationGame(settings: Settings) : BotBowsGame(settings) {
    private val doors: Set<SpaceStationDoor>
    private val doorMotors = mutableMapOf<SpaceStationDoor, DoorMotor>()

    init {
        val world = Main.WORLD_END

        doors = mutableSetOf(
            SpaceStationDoor(Location(world, 147.0, 74.0, 188.0), Axis.Z), // lower green
            SpaceStationDoor(Location(world, 147.0, 86.0, 180.0), Axis.Z), // upper green
            SpaceStationDoor(Location(world, 140.0, 86.0, 171.0), Axis.X), // green-orange
            SpaceStationDoor(Location(world, 136.0, 74.0, 199.0), Axis.X), // lower orange
            SpaceStationDoor(Location(world, 138.0, 86.0, 199.0), Axis.X), // upper orange
            SpaceStationDoor(Location(world, 147.0, 86.0, 212.0), Axis.Z), // upper blue
            SpaceStationDoor(Location(world, 134.0, 74.0, 242.0), Axis.X), // blue-red
            SpaceStationDoor(Location(world, 162.0, 74.0, 199.0), Axis.X), // lower red
            SpaceStationDoor(Location(world, 160.0, 86.0, 199.0), Axis.X), // upper red
        )
    }

    override fun startRound() {
        super.startRound()
        doors.forEach {
            it.open()
            doorMotors[it] = DoorMotor(it)
            scheduleDoor(it)
        }
    }

    override fun postRound(winningTeam: BotBowsTeam?, winScore: Int) {
        doorMotors.values.forEach { it.cancel() }
        doorMotors.clear()
        doors.forEach { it.open() }
        super.postRound(winningTeam, winScore)
    }

    private fun scheduleDoor(door: SpaceStationDoor) {
        var randomDelay = (1..5).random() + Main.WORLD_END.fullTime % 150 / 10 // they toggle each 5-10 - 35-40 seconds
        if (door.isOpen) { // the game switches between times when the doors are open ≈85% of the time and ≈15% of the time
            randomDelay = 45 - randomDelay
        }
        val motor = DoorMotor(door)
        motor.runTaskLater(Main.getPlugin(), randomDelay * 20)
        doorMotors[door] = motor
    }


    private inner class DoorMotor(val door: SpaceStationDoor) : BukkitRunnable() {
        override fun run() {
            door.toggle()
            scheduleDoor(door)
        }
    }
}
