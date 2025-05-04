package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamPiglin extends BotBowsTeam{

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -382.5, 22.0, -164.5, -90, 10),
            new Location(Main.WORLD, -382.5, 22.0, -167.5, -90, 10),
            new Location(Main.WORLD, -380.5, 22.0, -162.5, -90, 10),
            new Location(Main.WORLD, -380.5, 22.0, -169.5, -90, 10),
            new Location(Main.WORLD, -385.5, 22.0, -166.0, -90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -368.5, 41, -166.0, 0, 20);

    public TeamPiglin() {
        super("Piglin", NamedTextColor.GOLD, DyeColor.ORANGE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamPiglin(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
