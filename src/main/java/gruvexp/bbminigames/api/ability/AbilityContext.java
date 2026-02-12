package gruvexp.bbminigames.api.ability;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Location;

public interface AbilityContext {
    record Place(Location loc) {}
    record Melee(BotBowsPlayer defender) {}
}
