package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamBlaud extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -215.5, 22.0, -167.5, 90, 10),
            new Location(Main.WORLD, -215.5, 22.0, -164.5, 90, 10),
            new Location(Main.WORLD, -213.5, 22.0, -169.5, 90, 10),
            new Location(Main.WORLD, -213.5, 22.0, -162.5, 90, 10),
            new Location(Main.WORLD, -212.5, 22.0, -166.0, 90, 10)
    };

    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26, -123.5, 180, 10);

    public TeamBlaud() {
        super("Blaud", NamedTextColor.BLUE, DyeColor.BLUE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamBlaud(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
        postTeamSwap();
    }
}
