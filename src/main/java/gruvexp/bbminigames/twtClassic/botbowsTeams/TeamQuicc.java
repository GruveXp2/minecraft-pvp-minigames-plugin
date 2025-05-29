package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamQuicc extends BotBowsTeam{

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -385.5, 34, -377.5, -90, 10),
            new Location(Main.WORLD, -385.5, 34, -374.5, -90, 10),
            new Location(Main.WORLD, -387.5, 34, -379.5, -90, 10),
            new Location(Main.WORLD, -387.5, 34, -372.5, -90, 10),
            new Location(Main.WORLD, -388.5, 34, -376.0, -90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -357.0, 26, -418.5, 0, 20);

    public TeamQuicc() {
        super("Quicc", NamedTextColor.AQUA, DyeColor.CYAN, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamQuicc(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
