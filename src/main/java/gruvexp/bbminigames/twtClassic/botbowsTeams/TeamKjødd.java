package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamKjødd extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -211.5, 22.0, -377.5, 90, 10),
            new Location(Main.WORLD, -211.5, 22.0, -374.5, 90, 10),
            new Location(Main.WORLD, -212.5, 22.0, -379.5, 90, 10),
            new Location(Main.WORLD, -212.5, 22.0, -372.5, 90, 10),
            new Location(Main.WORLD, -210.3, 22.0, -376.0, 90, 10)
    };

    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26.0, -334.5, 180, 10);

    public TeamKjødd() {
        super("Kjødd", NamedTextColor.GOLD, DyeColor.ORANGE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamKjødd(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
