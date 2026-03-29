package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Hazard {

    private HazardChance hazardChance = getDefaultChance();
    private boolean isActive = false;

    public HazardChance getChance() {return hazardChance;}

    public void setChance(HazardChance chance) {hazardChance = chance;}

    public Map<BotBowsPlayer, BukkitRunnable> hazardTimers = new HashMap<>();

    public void triggerOnChance(Collection<BotBowsPlayer> players) {
        if (hazardChance.occurs()) {
            isActive = true;
            announce(players);
            trigger(players);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public abstract void init(Collection<BotBowsPlayer> players);
    public abstract HazardChance getDefaultChance();

    protected abstract void trigger(Collection<BotBowsPlayer> players); // hazarden starter
    protected abstract HazardMessage getAnnounceMessage();
    private void announce(Collection<BotBowsPlayer> players) {
        HazardMessage msg = getAnnounceMessage();
        players.forEach(p -> {
            p.avatar.message(Component.text(msg.chatHeader, NamedTextColor.DARK_RED)
                    .append(Component.text(" " + msg.chatDescription, NamedTextColor.RED)));
            p.avatar.showTitle(Component.text(msg.screenTitle, NamedTextColor.RED), 4);
        });
    }
    public abstract String getName();

    public abstract Component[] getDescription();

    public abstract String getActionDescription();

    public void end() {
        for (BukkitRunnable timer : hazardTimers.values()) { // stopp timerene
            timer.cancel();
        }
        hazardTimers.clear();
        isActive = false;
    } // stopper hazarden

    public record HazardMessage(
            String chatHeader,
            String chatDescription,
            String screenTitle
    ) {}
}
