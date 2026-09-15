package gruvexp.bbminigames

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

object ShutdownManager {
    private var shutdownTask: BukkitTask? = null
    private const val TEN_MINUTES_TICKS = 10 * 60 * 20L

    fun scheduleShutdown() {
        if (shutdownTask != null) return

        val plugin = Main.plugin
        plugin.logger.info("Last player left, auto closing in 10min")

        shutdownTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.logger.warning("Stopping server bc of inactivity")
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop")
        }, TEN_MINUTES_TICKS)
    }

    fun cancelShutdown() {
        shutdownTask?.let {
            it.cancel()
            shutdownTask = null
            Main.plugin.logger.info("A player joined, auto closing canceled")
        }
    }
}
