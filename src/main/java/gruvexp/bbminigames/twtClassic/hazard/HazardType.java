package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.function.Function;

public enum HazardType {

    STORM("Storm", Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.AQUA, StormHazard::new),
    EARTHQUAKE("Earthquake", Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.GOLD, EarthquakeHazard::new),
    GHOST("Ghost", Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, GhostHazard::new);

    public final String name;
    public final Material menuFillItem;
    public final NamedTextColor textColor;
    private final Function<Lobby, Hazard> factory;

    HazardType(String name, Material menuFillItem, NamedTextColor textColor, Function<Lobby, Hazard> factory) {
        this.name = name;
        this.menuFillItem = menuFillItem;
        this.textColor = textColor;
        this.factory = factory;
    }

    public Hazard createHazard(Lobby lobby) {
        return factory.apply(lobby);
    }
}
