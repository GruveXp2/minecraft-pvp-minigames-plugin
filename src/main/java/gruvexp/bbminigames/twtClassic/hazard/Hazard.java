package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.Settings;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public abstract class Hazard {

    private HazardChance hazardChance = HazardChance.TEN;
    private boolean isActive = false;
    protected final Settings settings;

    protected Hazard(Settings settings) {
        this.settings = settings;
    }

    public HazardChance getHazardChance() {return hazardChance;}

    public void setHazardChance(HazardChance chance) {hazardChance = chance;}

    public Map<Player, BukkitTask> hazardTimers = new HashMap<>();

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

    public void end() {
        for (BukkitTask timer : hazardTimers.values()) { // stopp timerene
            timer.cancel();
        }
        isActive = false;
    }; // stopper hazarden
}
