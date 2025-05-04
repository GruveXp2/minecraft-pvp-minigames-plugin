package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamGraut extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -268.5, 22.0, -274.5, -90, 10),
            new Location(Main.WORLD, -268.5, 22.0, -277.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -272.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -279.5, -90, 10),
            new Location(Main.WORLD, -271.5, 22.0, -276.0, -90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26, -318.5, 0, 10);

    public TeamGraut() {
        super("Graut", NamedTextColor.LIGHT_PURPLE, DyeColor.PURPLE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamGraut(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
