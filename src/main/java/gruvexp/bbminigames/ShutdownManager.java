package gruvexp.bbminigames;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class ShutdownManager {
    private static BukkitTask shutdownTask = null;
    private static final long TEN_MINUTES_TICKS = 10 * 60 * 20;

    public static void scheduleShutdown() {
        if (shutdownTask != null) return;

        Main plugin = Main.getPlugin();
        plugin.getLogger().info("Last player left, auto closing in 10min");

        shutdownTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getLogger().warning("Stopping server bc of inactivity");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
        }, TEN_MINUTES_TICKS);
    }

    public static void cancelShutdown() {
        if (shutdownTask != null) {
            shutdownTask.cancel();
            shutdownTask = null;
            Main.getPlugin().getLogger().info("A player joined, auto closing canceled");
        }
    }
}
