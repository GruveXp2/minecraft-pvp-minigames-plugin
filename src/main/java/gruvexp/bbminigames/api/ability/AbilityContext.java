package gruvexp.bbminigames.api.ability;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;

public interface AbilityContext {
    record Place(Location loc) {}
    record Melee(BotBowsPlayer defender) {}
    record Launch(Projectile projectile) {}
}
