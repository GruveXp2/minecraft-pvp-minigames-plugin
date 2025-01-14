package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamWacky extends BotBowsTeam {

    private static final Location[] SPAWN_POSITIONS = {
            new Location(Main.WORLD, -215.5, 22.0, -277.5, 90, 10),
            new Location(Main.WORLD, -215.5, 22.0, -274.5, 90, 10),
            new Location(Main.WORLD, -213.5, 22.0, -279.5, 90, 10),
            new Location(Main.WORLD, -213.5, 22.0, -272.5, 90, 10),
            new Location(Main.WORLD, -212.5, 22.0, -276.0, 90, 10)
    };
    private static final Location TRIBUNE_POSITION = new Location(Main.WORLD, -242.0, 26, -233.5, 180, 10);

    public TeamWacky() {
        super("Wacky", NamedTextColor.GREEN, DyeColor.LIME, SPAWN_POSITIONS, TRIBUNE_POSITION);
    }

    public TeamWacky(BotBowsTeam otherTeam) {
        this();
        players = otherTeam.players;
    }
}
