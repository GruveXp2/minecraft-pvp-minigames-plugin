package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.function.Supplier;

public enum HazardType {

    STORM("Storm", HazardChance.TEN, Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.AQUA, StormHazard::new),
    EARTHQUAKE("Earthquake", HazardChance.FIVE, Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.GOLD, EarthquakeHazard::new),
    GHOST("Ghost", HazardChance.DISABLED, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, GhostHazard::new);

    public final String name;
    public final HazardChance defaultChance;
    public final Material menuFillItem;
    public final NamedTextColor textColor;
    private final Supplier<Hazard> supplier;

    HazardType(String name, HazardChance defaultChance, Material menuFillItem, NamedTextColor textColor, Supplier<Hazard> supplier) {
        this.name = name;
        this.defaultChance = defaultChance;
        this.menuFillItem = menuFillItem;
        this.textColor = textColor;
        this.supplier = supplier;
    }

    public Hazard createHazard() {
        return supplier.get();
    }
}
