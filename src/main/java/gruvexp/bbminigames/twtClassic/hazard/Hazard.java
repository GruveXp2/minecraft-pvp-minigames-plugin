package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.Lobby;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public abstract class Hazard {

    private HazardChance hazardChance = HazardChance.TEN;
    private boolean isActive = false;
    protected final Lobby lobby;

    protected Hazard(Lobby lobby) {
        this.lobby = lobby;
    }

    public HazardChance getChance() {return hazardChance;}

    public void setChance(HazardChance chance) {hazardChance = chance;}

    public Map<Player, BukkitRunnable> hazardTimers = new HashMap<>();

    public void triggerOnChance() {
        if (hazardChance.occurs()) {
            isActive = true;
            trigger();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    protected abstract void trigger(); // hazarden starter

    public abstract String getName();

    public abstract Component[] getDescription();

    public abstract String getActionDescription();

    public void end() {
        for (BukkitRunnable timer : hazardTimers.values()) { // stopp timerene
            timer.cancel();
        }
        hazardTimers.clear();
        isActive = false;
    }; // stopper hazarden
}
