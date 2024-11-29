package gruvexp.bbminigames.twtClassic;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Cooldowns { // Inneholder shifting og storm logic

    public static Map<Player, BukkitTask> sneakRunnables = new HashMap<>();
    public static Map<Player, Integer> sneakCooldowns = new HashMap<>();

    public static void CoolDownInit(Collection<BotBowsPlayer> players) {
        for (BotBowsPlayer p : players) {
            sneakCooldowns.put(p.PLAYER, 0);
        }
    }

}
