package gruvexp.bbminigames

import gruvexp.bbminigames.commands.*
import gruvexp.bbminigames.database.StatsDatabase
import gruvexp.bbminigames.listeners.*
import gruvexp.bbminigames.service.BattlePresetService
import gruvexp.bbminigames.service.StatsService
import gruvexp.bbminigames.sumo.FloorListener
import gruvexp.bbminigames.sumo.SumoCommand
import gruvexp.bbminigames.sumo.SumoTabCompleter
import gruvexp.bbminigames.twtClassic.BotBows.getLobby
import gruvexp.bbminigames.twtClassic.BotBows.lobbies
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class Main : JavaPlugin() {
    lateinit var presetService: BattlePresetService
        private set
    lateinit var statsService: StatsService
        private set

    override fun onEnable() {
        plugin = this
        logger.info("BotBows plugin enabled!")
        listOf(
            MenuListener(),
            DamageListener(),
            MovementListener(),
            JoinLeaveListener(),
            RightClickListener(),
            ShiftListener(),
            SwitchSpectator(),
            AbilityListener(),
            ItemListener(),
            FloorListener()
        ).forEach { server.pluginManager.registerEvents(it, this) }

        getCommand("menu")!!.setExecutor(MenuCommand())
        getCommand("settings")!!.setExecutor(SettingsCommand())
        getCommand("botbows")!!.setExecutor(BotBowsCommand())
        getCommand("botbows")!!.tabCompleter = BotBowsTabCompleter()
        getCommand("sumo")!!.setExecutor(SumoCommand())
        getCommand("sumo")!!.tabCompleter = SumoTabCompleter()
        getCommand("test")!!.setExecutor(TestCommand())
        getCommand("test")!!.tabCompleter = TestTabCompleter()
        WORLD = Bukkit.getWorld("BotBows (S2E1)")!!
        WORLD_END = Bukkit.getWorld("BotBows (S2E1)_the_end")!!
        WORLD_SPAWN_LOBBY = Location(WORLD, -129.0, 39.0, -197.0)

        val dbFolder = File(dataFolder, "db")
        statsService = StatsService(this, StatsDatabase(dbFolder))
        presetService = BattlePresetService()
        presetService.loadPresetsFromFile()
        Thread { startSocketServer() }.start() // Start the server in a new thread to avoid blocking the main thread
    }

    override fun onDisable() {
        logger.info("Disabling BotBows plugin")
        for (lobby in lobbies) {
            if (lobby.isGameActive) {
                logger.info("Stopping active game...")
                lobby.botBowsGame!!.endGame()
                lobby.botBowsGame!!.cleanupResultDisplay()
            } else {
                lobby.reset()
            }
        }
    }

    private fun startSocketServer() { // TODO: use something better than sockets, since were on the same pc, we can just use intra-process comunication
        try {
            ServerSocket(DISCORD_BOT_PORT).use { serverSocket ->
                logger.info("Server listening on port $DISCORD_BOT_PORT")
                while (true) {
                    try {
                        serverSocket.accept().use { clientSocket ->
                            BufferedReader(InputStreamReader(clientSocket.getInputStream())).use { input ->
                                BufferedWriter(
                                    OutputStreamWriter(clientSocket.getOutputStream())
                                ).use { output ->
                                    val command = input.readLine()
                                    //getLogger().info("Received command: " + command);
                                    if (command == null || command.trim { it <= ' ' }.isEmpty()) return
                                    if (command.startsWith("@")) {
                                        if (command == "@ping") {
                                            if (getLobby(0).isGameActive) { // TODO: shoudnt just check lobby 1, but also the others. either show stats from current battle, or if many, show "x battles ongoing"
                                                val teamSizes = getLobby(0).settings.let { it.team1.size() to it.team2.size() }
                                                output.write("BotBows ${teamSizes.first}v${teamSizes.second} match ongoing")
                                            } else {
                                                output.write("BotBows: ${Bukkit.getOnlinePlayers().size} online")
                                            }
                                            output.newLine()
                                            output.flush()
                                        }
                                    } else { // a minecraft command
                                        val latch = CountDownLatch(1)

                                        Bukkit.getScheduler().runTask(
                                            this,
                                            Runnable { // Schedule the command execution on the main thread
                                                try {
                                                    // Execute the command on the server console
                                                    val console = Bukkit.getServer().consoleSender
                                                    val result = executeCommand(console, command)

                                                    synchronized(output) { // Ensure safe access to the BufferedWriter
                                                        try {
                                                            // Send the result back to the client
                                                            output.write(result)
                                                            output.newLine()
                                                            output.flush()
                                                            //getLogger().info("The result of the command is: \n" + result + "\n======");
                                                        } catch (e: IOException) {
                                                            logger.severe("Error sending result to client: ${e.message}")
                                                        }
                                                    }
                                                } finally {
                                                    latch.countDown() // Signal that the task is complete
                                                }
                                            })

                                        // Wait for the task to complete before closing the resources
                                        try {
                                            latch.await(1, TimeUnit.SECONDS) // if the server lags so much it takes over a second to run the command, then it will quit waiting
                                        } catch (e: InterruptedException) {
                                            logger.severe("Waiting for task completion interrupted: ${e.message}")
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        logger.severe("Error handling client: ${e.message}")
                    }
                }
            }
        } catch (e: IOException) {
            logger.severe("Could not listen on port $DISCORD_BOT_PORT")
            e.printStackTrace()
        }
    }

    private fun executeCommand(console: ConsoleCommandSender, command: String): String {
        val outputByteStream = ByteArrayOutputStream()
        val originalOut = System.out

        try {
            // Redirect system output to capture command output
            System.setOut(PrintStream(outputByteStream))

            // Execute the command
            Bukkit.dispatchCommand(console, command)

            // Restore original system output
            System.setOut(originalOut)

            // Return the captured output
            return "$outputByteStream".trim { it <= ' ' }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error capturing command output: ${e.message}"
        }
    }

    companion object {
        lateinit var plugin: Main
            private set
        lateinit var WORLD: World
        lateinit var WORLD_END: World
        lateinit var WORLD_SPAWN_LOBBY: Location
        private const val DISCORD_BOT_PORT = 25566 // Port used to communicate with the discord bot
    }
}
