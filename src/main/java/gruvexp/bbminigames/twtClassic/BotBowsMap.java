package gruvexp.bbminigames.twtClassic;

import com.google.common.collect.ImmutableSet;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;

public enum BotBowsMap {
    CLASSIC_ARENA(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST)),
    ICY_RAVINE(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST)),
    ROYAL_MAP(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST)),
    STEAMPUNK(ImmutableSet.of(HazardType.STORM, HazardType.EARTHQUAKE, HazardType.GHOST)),
    PIGLIN_HIDEOUT(ImmutableSet.of(HazardType.EARTHQUAKE, HazardType.GHOST)),

    INSIDE_BOTBASE(ImmutableSet.of(HazardType.STORM, HazardType.GHOST)),
    OUTSIDE_BOTBASE(ImmutableSet.of(HazardType.STORM, HazardType.GHOST)),
    ROCKET_FOREST(ImmutableSet.of(HazardType.STORM, HazardType.GHOST)),
    ROCKET(ImmutableSet.of(HazardType.GHOST)),
    SPACE_STATION(ImmutableSet.of(HazardType.GHOST));

    public final ImmutableSet<HazardType> allowedHazards;

    BotBowsMap(ImmutableSet<HazardType> allowedHazards) {
        this.allowedHazards = allowedHazards;
    }
}
