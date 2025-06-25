package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamWarm extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD_END, 123.5, 75, 225.5, 170   ,  5),
            new Location(Main.WORLD_END, 186.5, 88, 200.5, 85    , 10),
            new Location(Main.WORLD_END, 117.5, 80, 211.5, 153.5f, 35),
            new Location(Main.WORLD_END, 186.5, 88, 202.5, 95    , 10),
            new Location(Main.WORLD_END, 116.5, 75, 226.5, 170   ,  5)
    };

    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD_END, 173.5, 89.5, 201.5, 90, 10);

    public TeamWarm() {
        super("Warm", NamedTextColor.GOLD, DyeColor.ORANGE, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamWarm(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
