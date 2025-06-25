package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamCold extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD_END, 128.5, 88.0, 267.5, 180    , 10),
            new Location(Main.WORLD_END, 165.5, 74.5, 173.5, 90     , -5),
            new Location(Main.WORLD_END, 126.5, 90.0, 264.5, -163.5f, 30),
            new Location(Main.WORLD_END, 169.5, 22.0, 173.5, 90     , 10),
            new Location(Main.WORLD_END, 130.5, 89.0, 262.0, 150    , 35)
    };

    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD_END, 149.5, 87, 168.5, 0, 10);

    public TeamCold() {
        super("Cold", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamCold(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
