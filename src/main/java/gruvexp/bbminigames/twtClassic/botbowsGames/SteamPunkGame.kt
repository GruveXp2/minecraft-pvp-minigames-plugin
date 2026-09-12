package gruvexp.bbminigames.twtClassic.botbowsGames

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.mechanics.*
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3i

class SteamPunkGame(settings: Settings) : BotBowsGame(settings) {
    private val steamPipes = mutableSetOf<SteamPipe>()
    private val pipeChunks = mutableMapOf<Chunk, MutableSet<SteamPipe>>()
    private var steamPipeMotor: SteamPipeMotor? = null // responsible for powering the pipes by giving them 20 ticks/s

    private val hatches: Set<Hatch>
    private val hatchMotors = mutableMapOf<Hatch, HatchMotor>()

    private val impellers = mutableSetOf<Impeller>()
    private val impellerChunks = mutableMapOf<Chunk, MutableSet<Impeller>>()
    private var impellerMotor: ImpellerMotor? = null // responsible for powering the pipes by giving them 20 ticks/s

    private val gates: Set<Gate>
    private var gateMotor: GateMotor? = null

    private val bigWheels: Set<Gear>

    private val rotors: Set<Rotor> // spinning blades that hold up the upper parts of the arena

    init {
        val world = Main.WORLD
        // steam pipes
        // copper -> weathered
        registerSteamPipe(
            SteamPipe(
                true, listOf(
                    Location(world, -350.8, 17.0, -398.5),
                    Location(world, -371.5, 17.0, -398.5),
                    Location(world, -371.5, 17.0, -395.8)
                ), Axis.X, Axis.Z
            )
        )
        // copper -> surface
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -351.2, 17.0, -394.5),
                    Location(world, -350.5, 17.0, -394.5),
                    Location(world, -350.5, 17.9, -387.5),
                    Location(world, -350.5, 25.0, -387.8),
                    Location(world, -350.5, 25.0, -384.5),
                    Location(world, -357.5, 25.0, -384.5),
                    Location(world, -357.5, 26.9, -384.5)
                ), Axis.X, Axis.Y
            )
        )

        // exposed -> exposed
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -352.2, 18.0, -368.5),
                    Location(world, -340.5, 18.0, -368.5)
                ), Axis.X, Axis.X
            )
        )
        // exposed -> weathered
        registerSteamPipe(
            SteamPipe(
                true, listOf(
                    Location(world, -352.2, 18.0, -370.5),
                    Location(world, -350.5, 18.0, -370.5),
                    Location(world, -350.5, 18.0, -379.5),
                    Location(world, -361.2, 18.0, -379.5)
                ), Axis.X, Axis.X
            )
        )
        // exposed -> oxidized
        registerSteamPipe(
            SteamPipe(
                true, listOf(
                    Location(world, -346.5, 17.5, -360.2),
                    Location(world, -346.5, 17.0, -353.5),
                    Location(world, -365.2, 17.5, -353.5)
                ), Axis.Z, Axis.X
            )
        )
        // exposed -> surface
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -351.2, 17.0, -357.5),
                    Location(world, -350.5, 17.0, -357.5),
                    Location(world, -350.5, 17.9, -364.5),
                    Location(world, -350.5, 25.5, -364.2),
                    Location(world, -350.5, 25.0, -367.5),
                    Location(world, -356.5, 25.0, -367.5),
                    Location(world, -356.5, 26.9, -367.5)
                ), Axis.X, Axis.Y
            )
        )

        // surface_copper -> gate_left
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -333.8, 25.0, -369.5),
                    Location(world, -338.3, 25.0, -369.5),
                    Location(world, -338.2, 25.0, -367.5),
                    Location(world, -342.5, 25.0, -367.5),
                    Location(world, -343.5, 25.0, -361.5),
                    Location(world, -342.5, 31.9, -361.2),
                    Location(world, -342.5, 31.0, -358.5),
                    Location(world, -347.5, 31.0, -358.5)
                ), Axis.Z, Axis.X
            )
        )
        // surface_copper -> gate_right
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -333.8, 25.0, -382.5),
                    Location(world, -338.3, 25.0, -382.5),
                    Location(world, -338.2, 25.0, -384.5),
                    Location(world, -342.5, 25.0, -384.5),
                    Location(world, -343.5, 25.0, -390.5),
                    Location(world, -342.5, 31.9, -390.8),
                    Location(world, -342.5, 31.0, -393.5),
                    Location(world, -347.5, 31.0, -393.5)
                ), Axis.Z, Axis.X
            )
        )

        // surface_oxidized -> gate_left
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -380.2, 25.0, -369.5),
                    Location(world, -375.7, 25.0, -369.5),
                    Location(world, -375.8, 25.0, -367.5),
                    Location(world, -371.5, 25.0, -367.5),
                    Location(world, -371.5, 25.0, -361.5),
                    Location(world, -371.5, 31.9, -361.2),
                    Location(world, -371.5, 31.0, -358.5),
                    Location(world, -366.5, 31.0, -358.5)
                ), Axis.Z, Axis.X
            )
        )
        // surface_oxidized -> gate_right
        registerSteamPipe(
            SteamPipe(
                false, listOf(
                    Location(world, -380.2, 25.0, -382.5),
                    Location(world, -375.7, 25.0, -382.5),
                    Location(world, -375.8, 25.0, -384.5),
                    Location(world, -371.5, 25.0, -384.5),
                    Location(world, -371.5, 25.0, -390.5),
                    Location(world, -371.5, 31.9, -390.8),
                    Location(world, -371.5, 31.0, -393.5),
                    Location(world, -366.5, 31.0, -393.5)
                ), Axis.Z, Axis.X
            )
        )

        // hatches
        hatches = setOf(
            Hatch(1, Location(world, -357.0, 21.0, -395.0), StructureRotation.COUNTERCLOCKWISE_90, "copper_hatch_exposed"), // copper
            Hatch(2, Location(world, -358.0, 21.0, -395.0), StructureRotation.CLOCKWISE_90, "copper_hatch_exposed"), // weathered
            Hatch(1, Location(world, -357.0, 21.0, -358.0), StructureRotation.COUNTERCLOCKWISE_90, "copper_hatch_weathered"), // exposed
            Hatch(2, Location(world, -358.0, 21.0, -358.0), StructureRotation.CLOCKWISE_90, "copper_hatch_weathered"), // oxidized
        )


        // impellers
        registerImpeller(
            Impeller(1, "copper_impeller_oxidized", Location(world, -371.0, 16.0, -381.0), 2f)
        )
        registerImpeller(
            Impeller(1, "copper_impeller_exposed", Location(world, -371.0, 16.0, -387.0), -4f) // center
        )
        registerImpeller(
            Impeller(2, "copper_impeller_exposed", Location(world, -371.0, 16.0, -392.0), 4f) // next to pipe
        )

        // gates
        val gateSize = Vector3i(3, 7, 7)
        val gateFramesSrc = Location(world, -400.0, 1.0, -327.0)

        gates = setOf(
            Gate( // copper
                gateFramesSrc, 3, gateSize, Location(world, -347.0, 22.0, -397.0), 2, true,
                setOf(
                    Gear(1, Location(world, -348.0, 26.0, -399.0), StructureRotation.NONE, "copper_wheel", 25f),
                    Gear(2, Location(world, -344.0, 26.0, -399.0), StructureRotation.NONE, "copper_wheel", 25f),
                    Gear(3, Location(world, -348.0, 24.0, -389.0), StructureRotation.NONE, "copper_wheel", -12f)
                )
            ),
            Gate( // exposed
                gateFramesSrc.clone().add(12.0, 0.0, 0.0), 3, gateSize, Location(world, -347.0, 22.0, -362.0), 3, false,
                setOf(
                    Gear(1, Location(world, -348.0, 26.0, -354.0), StructureRotation.NONE, "copper_wheel_exposed", 16f, 2),
                    Gear(2, Location(world, -344.0, 26.0, -354.0), StructureRotation.NONE, "copper_wheel_exposed", 16f, 2),
                    Gear(3, Location(world, -348.0, 24.0, -364.0), StructureRotation.NONE, "copper_wheel_exposed", -8f, 2)
                )
            ),
            Gate( // weathered
                gateFramesSrc.clone().add(24.0, 0.0, 0.0), 3, gateSize, Location(world, -370.0, 22.0, -397.0), 4, false,
                setOf(
                    Gear(1, Location(world, -367.0, 26.0, -399.0), StructureRotation.NONE, "copper_wheel_weathered", 9f, 3),
                    Gear(2, Location(world, -371.0, 26.0, -399.0), StructureRotation.NONE, "copper_wheel_weathered", 9f, 3),
                    Gear(3, Location(world, -367.0, 24.0, -389.0), StructureRotation.NONE, "copper_wheel_weathered", -4.5f, 3)
                )
            ),
            Gate( // oxidized
                gateFramesSrc.clone().add(36.0, 0.0, 0.0), 3, gateSize, Location(world, -370.0, 22.0, -362.0), 6, true,
                setOf(
                    Gear(1, Location(world, -367.0, 26.0, -354.0), StructureRotation.NONE, "copper_wheel_oxidized", 5f, 4),
                    Gear(2, Location(world, -371.0, 26.0, -354.0), StructureRotation.NONE, "copper_wheel_oxidized", 5f, 4),
                    Gear(3, Location(world, -367.0, 24.0, -364.0), StructureRotation.NONE, "copper_wheel_oxidized", -2.5f, 4)
                )
            )
        )

        bigWheels = setOf(
            Gear(1, Location(world, -339.0, 21.0, -396.0), StructureRotation.NONE, "big_copper_wheel", 8f, 5),
            Gear(1, Location(world, -339.0, 21.0, -357.0), StructureRotation.NONE, "big_copper_wheel_exposed", -5f, 5),
            Gear(1, Location(world, -376.0, 21.0, -396.0), StructureRotation.NONE, "big_copper_wheel_weathered", 3f, 5),
            Gear(1, Location(world, -376.0, 21.0, -357.0), StructureRotation.NONE, "big_copper_wheel_oxidized", -2f, 5),
        )

        rotors = setOf(
            Rotor(1, Location(world, -332.0, 39.0, -391.0), "copper_rotor", 50f, 4),
            Rotor(2, Location(world, -336.0, 39.0, -391.0), "copper_rotor", 50f, 4),
            Rotor(3, Location(world, -344.0, 39.0, -395.0), "copper_rotor", 50f, 4),
            Rotor(4, Location(world, -344.0, 39.0, -392.0), "copper_rotor", 50f, 4),
            Rotor(5, Location(world, -343.0, 39.0, -381.0), "copper_rotor", 50f, 4),
            Rotor(6, Location(world, -343.0, 39.0, -372.0), "copper_rotor", 50f, 4),
            Rotor(1, Location(world, -332.0, 39.0, -362.0), "copper_rotor_exposed", 37f, 5),
            Rotor(2, Location(world, -336.0, 39.0, -362.0), "copper_rotor_exposed", 37f, 5),
            Rotor(3, Location(world, -344.0, 39.0, -358.0), "copper_rotor_exposed", 37f, 5),
            Rotor(4, Location(world, -344.0, 39.0, -361.0), "copper_rotor_exposed", 37f, 5),
            Rotor(5, Location(world, -351.0, 39.0, -389.0), "copper_rotor_exposed", 37f, 5),
            Rotor(6, Location(world, -358.0, 39.0, -391.0), "copper_rotor_exposed", 37f, 5),
            Rotor(7, Location(world, -358.0, 39.0, -394.0), "copper_rotor_exposed", 37f, 5),
            Rotor(1, Location(world, -351.0, 39.0, -364.0), "copper_rotor_weathered", 25f, 7),
            Rotor(2, Location(world, -357.0, 39.0, -359.0), "copper_rotor_weathered", 25f, 7),
            Rotor(3, Location(world, -357.0, 39.0, -362.0), "copper_rotor_weathered", 25f, 7),
            Rotor(4, Location(world, -364.0, 39.0, -389.0), "copper_rotor_weathered", 25f, 7),
            Rotor(5, Location(world, -371.0, 39.0, -395.0), "copper_rotor_weathered", 25f, 7),
            Rotor(6, Location(world, -371.0, 39.0, -392.0), "copper_rotor_weathered", 25f, 7),
            Rotor(7, Location(world, -372.0, 39.0, -381.0), "copper_rotor_weathered", 25f, 7),
            Rotor(1, Location(world, -363.0, 39.0, -364.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(2, Location(world, -371.0, 39.0, -358.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(3, Location(world, -371.0, 39.0, -361.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(4, Location(world, -372.0, 39.0, -372.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(5, Location(world, -379.0, 39.0, -391.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(6, Location(world, -383.0, 39.0, -391.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(7, Location(world, -383.0, 39.0, -362.0), "copper_rotor_oxidized", 18f, 9),
            Rotor(8, Location(world, -379.0, 39.0, -362.0), "copper_rotor_oxidized", 18f, 9),
        )
    }

    private fun registerSteamPipe(steamPipe: SteamPipe) {
        steamPipes.add(steamPipe)
        steamPipe.tickedChunks.forEach {
            pipeChunks.getOrPut(it, ::mutableSetOf ).add(steamPipe)
        }
    }

    private fun registerImpeller(impeller: Impeller) {
        impellers.add(impeller)
        impeller.tickedChunks.forEach {
            impellerChunks.getOrPut(it, ::mutableSetOf).add(impeller)
        }
    }

    override fun startGame() {
        super.startGame()
        bigWheels.forEach { it.rotate(360f * 1225) } // 1225 POINTs
        rotors.forEach { it.startRotating() }
    }

    override fun startRound() {
        super.startRound()
        val plugin = Main.getPlugin()
        steamPipeMotor = SteamPipeMotor(steamPipes)
            .apply { runTaskTimer(plugin, 200, 1) }
        hatches.forEach {
            hatchMotors[it] = HatchMotor(it)
            scheduleHatch(it)
        }
        impellerMotor = ImpellerMotor(impellers).apply {
            runTaskTimer(plugin, 200, 1)
        }
        gateMotor = GateMotor(gates).apply {
            runTaskTimer(plugin, 200, DOOR_TOGGLE_DELAY.toLong())
        }
    }

    override fun postRound(winningTeam: BotBowsTeam?, winScore: Int) {
        stopMotors()
        super.postRound(winningTeam, winScore)
    }

    override fun postGame(winningTeam: BotBowsTeam?) {
        if (steamPipeMotor != null) stopMotors() // stop motors unless they already got stopped in postRound()

        bigWheels.forEach { it.stop() }
        rotors.forEach { it.stop() }
        super.postGame(winningTeam)
    }

    private fun stopMotors() {
        steamPipeMotor?.cancel()
        steamPipeMotor = null
        hatchMotors.values.forEach { it.cancel() }
        hatchMotors.clear()
        impellerMotor?.cancel()
        impellerMotor = null
        gateMotor?.cancel()
        gateMotor = null
    }

    override fun handleMovement(e: PlayerMoveEvent) {
        super.handleMovement(e)
        val p = e.player
        val chunk = p.chunk

        pipeChunks[chunk]?.forEach { it.checkProximity(p) }

        impellerChunks[chunk]?.forEach { it.checkProximity(p) }
    }

    private fun scheduleHatch(hatch: Hatch) {
        val randomDelay = (2..16).random() // they toggle each 2-16 seconds
        val motor = HatchMotor(hatch).apply {
            runTaskLater(Main.getPlugin(), randomDelay * 20L)
        }
        hatchMotors[hatch] = motor
    }


    private class SteamPipeMotor(val steamPipes: Set<SteamPipe>) : BukkitRunnable() {
        override fun run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            steamPipes.forEach { it.tick() }
        }
    }

    private inner class HatchMotor(val hatch: Hatch) : BukkitRunnable() {
        override fun run() {
            hatch.toggle()
            scheduleHatch(hatch)
        }
    }

    private class ImpellerMotor(val impellers: Set<Impeller>) : BukkitRunnable() {
        override fun run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            impellers.forEach { it.tick() }
        }
    }

    private class GateMotor(val gates: Set<Gate>) : BukkitRunnable() {
        override fun run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            gates.forEach { it.toggle() }
        }
    }

    companion object {
        const val DOOR_TOGGLE_DELAY: Int = 15 * 20
    }
}
