package gruvexp.bbminigames;

import gruvexp.bbminigames.commands.*;
import gruvexp.bbminigames.listeners.*;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {

    private static Main PLUGIN;
    public static World WORLD;
    private static final int PORT = 25566; // Port used to communicate with the discord bot
    public static Main getPlugin() {
        return PLUGIN;
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        getLogger().info("BotBows plugin enabled!");
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new HitListener(), this);
        getServer().getPluginManager().registerEvents(new MovementListener(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new RightClickListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftListener(), this);
        getServer().getPluginManager().registerEvents(new SwitchSpectator(), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(), this);

        getCommand("menu").setExecutor(new MenuCommand());
        getCommand("settings").setExecutor(new SettingsCommand());
        getCommand("start").setExecutor(new StartCommand());
        getCommand("leave").setExecutor(new LeaveCommand());
        getCommand("stopgame").setExecutor(new StopGameCommand());
        getCommand("test").setExecutor(new TestCommand());
        getCommand("test").setTabCompleter(new TestTabCompleter());
        WORLD = Bukkit.getWorld("BotBows (S2E1)");
        BotBows.init();
        BotBowsPlayer.armorInit();
        new Thread(this::startSocketServer).start(); // Start the server in a new thread to avoid blocking the main thread
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling BotBows plugin");
        for (Lobby lobby : BotBows.getLobbies()) {
            if (lobby.isGameActive()) {
                getLogger().info("Stopping active game...");
                lobby.botBowsGame.endGame();
            } else {
                lobby.reset();
            }
        }

    }

    private void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            getLogger().info("Server listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                    String command = in.readLine();
                    //getLogger().info("Received command: " + command);
                    if (command == null || command.trim().isEmpty()) return;
                    if (command.startsWith("@")) {
                        if (command.equals("@ping")) {
                            if (BotBows.getLobby(0).isGameActive()) {
                                out.write("BotBows " + BotBows.getLobby(0).settings.team1.size() + "v" + BotBows.getLobby(1).settings.team2.size() + " match ongoing");
                            } else {
                                out.write("BotBows: " + Bukkit.getOnlinePlayers().size() + " online");
                            }
                            out.newLine();
                            out.flush();
                        }
                    } else { // a minecraft command

                        CountDownLatch latch = new CountDownLatch(1);

                        Bukkit.getScheduler().runTask(this, () -> { // Schedule the command execution on the main thread
                            try {
                                // Execute the command on the server console
                                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                                String result = executeCommand(console, command);

                                synchronized (out) { // Ensure safe access to the BufferedWriter
                                    try {
                                        // Send the result back to the client
                                        out.write(result);
                                        out.newLine();
                                        out.flush();
                                        //getLogger().info("The result of the command is: \n" + result + "\n======");
                                    } catch (IOException e) {
                                        getLogger().severe("Error sending result to client: " + e.getMessage());
                                    }
                                }
                            } finally {
                                latch.countDown(); // Signal that the task is complete
                            }
                        });

                        // Wait for the task to complete before closing the resources
                        try {
                            latch.await(1, TimeUnit.SECONDS); // if the server lags so much it takes over a second to run the command, then it will quit waiting
                        } catch (InterruptedException e) {
                            getLogger().severe("Waiting for task completion interrupted: " + e.getMessage());
                        }
                    }
                    //getLogger().warning("The socket will close now");
                } catch (IOException e) {
                    getLogger().severe("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            getLogger().severe("Could not listen on port " + PORT);
            e.printStackTrace();
        }
    }

    private String executeCommand(ConsoleCommandSender console, String command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            // Redirect system output to capture command output
            System.setOut(new PrintStream(baos));

            // Execute the command
            Bukkit.dispatchCommand(console, command);

            // Restore original system output
            System.setOut(originalOut);

            // Return the captured output
            return baos.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error capturing command output: " + e.getMessage();
        }
    }
}
