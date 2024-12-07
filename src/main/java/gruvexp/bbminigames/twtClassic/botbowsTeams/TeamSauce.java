package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamSauce extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -268.5, 22.0, -164.5, -90, 10),
            new Location(Main.WORLD, -268.5, 22.0, -167.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -162.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -169.5, -90, 10),
            new Location(Main.WORLD, -271.5, 22.0, -166.0, -90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26, -208.5, 0, 10);

    public TeamSauce() {
        super("Sauce", NamedTextColor.RED, DyeColor.RED, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamSauce(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
        postTeamSwap();
    }
}
