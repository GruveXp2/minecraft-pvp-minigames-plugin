package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamGoofy extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -268.5, 22.0, -377.5, -90, 10),
            new Location(Main.WORLD, -268.5, 22.0, -374.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -379.5, -90, 10),
            new Location(Main.WORLD, -270.5, 22.0, -372.5, -90, 10),
            new Location(Main.WORLD, -271.3, 22.0, -376.0, -90, 10)
    };

    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26.0, -417.5, 0, 10);

    public TeamGoofy() {
        super("Goofy", NamedTextColor.DARK_GREEN, DyeColor.GREEN, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamGoofy(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
