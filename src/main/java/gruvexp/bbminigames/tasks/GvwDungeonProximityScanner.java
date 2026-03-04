package gruvexp.bbminigames.tasks;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class GvwDungeonProximityScanner extends BukkitRunnable {

    private static final Location BOUNDING_BOX_MIN = new Location(Main.WORLD, -259, 12, -304);
    private static final Location BOUNDING_BOX_MAX = new Location(Main.WORLD, -240, 19, -283);
    public final BotBowsPlayer bp;
    private boolean isInDungeon = false;

    public GvwDungeonProximityScanner(BotBowsPlayer bp) {
        this.bp = bp;
    }

    @Override
    public void run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
        if (isInDungeon != isInsideBoundingBox(bp.getLocation(), BOUNDING_BOX_MIN, BOUNDING_BOX_MAX)) {
            //BotBowsManager.debugMessage(STR."\{p.getName()} moved thru the bounding box");
        }
        isInDungeon = isInsideBoundingBox(bp.getLocation(), BOUNDING_BOX_MIN, BOUNDING_BOX_MAX);
    }

    public static boolean isInsideBoundingBox(Location loc, Location min, Location max) {
        return loc.getX() >= min.getX() && loc.getX() < max.getX() &&
                loc.getY() >= min.getY() && loc.getY() < max.getY() &&
                loc.getZ() >= min.getZ() && loc.getZ() < max.getZ();
    }

    public boolean isInDungeon() {return isInDungeon;}
}
