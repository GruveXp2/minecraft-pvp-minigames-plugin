package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamBlocc extends BotBowsTeam{

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -327.5, 34, -377.5, 90, 10),
            new Location(Main.WORLD, -327.5, 34, -374.5, 90, 10),
            new Location(Main.WORLD, -325.5, 34, -379.5, 90, 10),
            new Location(Main.WORLD, -325.5, 34, -372.5, 90, 10),
            new Location(Main.WORLD, -324.5, 34, -376.0, 90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -357.0, 26, -333.3, 180, 20);

    public TeamBlocc() {
        super("Blocc", NamedTextColor.GOLD, DyeColor.ORANGE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamBlocc(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
