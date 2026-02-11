package gruvexp.bbminigames.api.ability;

import org.bukkit.Location;

public interface AbilityContext {
    record Place(Location loc) {}
}
