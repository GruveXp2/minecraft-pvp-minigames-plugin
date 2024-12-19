package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.tasks.GvwDungeonProximityScanner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class DungeonGhoster {

    private static final Location GREEN_BOUNDING_BOX_MIN = new Location(Main.WORLD, -250, 15, -297);
    private static final Location GREEN_BOUNDING_BOX_MAX = new Location(Main.WORLD, -245, 19, -287);
    private static final Location PURPLE_BOUNDING_BOX_MIN = new Location(Main.WORLD, -255, 16, -299);
    private static final Location PURPLE_BOUNDING_BOX_MAX = new Location(Main.WORLD, -251, 19, -288);

    private static final Location PURPLE_ENTER_BB_MIN = new Location(Main.WORLD, -250, 16, -287.3);
    private static final Location PURPLE_ENTER_BB_MAX = new Location(Main.WORLD, -247, 18, -286);

    private static final Location GREEN_ENTER_BB_MIN = new Location(Main.WORLD, -254, 16, -300);
    private static final Location GREEN_ENTER_BB_MAX = new Location(Main.WORLD, -251, 18, -287.7);

    private Section section = Section.OUTSIDE;
    private ArmorStand AS_1;
    private ArmorStand AS_2;

    private final Player PLAYER;
    private final BotBowsPlayer BOTBOWSPLAYER;
    private Location prevLoc;

    public DungeonGhoster(BotBowsPlayer p) {
        BOTBOWSPLAYER = p;
        PLAYER = p.player;
        prevLoc = PLAYER.getLocation();
        updateArmorStandsAndSection();
    }

    public void handleMovement() {
        Location currentLoc = PLAYER.getLocation();
        if (currentLoc.equals(prevLoc)) {
            Bukkit.getLogger().info("Somehow handleMovement() was called when the player didnt move (bug)");
            return; // No movement, no need to process
        }

        if (isInSameSection()) {
            updateArmorStandsPosition(currentLoc);
        } else {
            //String oldSection = section.toString();
            updateArmorStandsAndSection();
            //debugMessage(STR."\{PLAYER.getName()} moved: \{ChatColor.WHITE}\{oldSection} -> \{section.toString()}");
        }
        prevLoc = currentLoc;
    }

    private void updateArmorStandsPosition(Location currentLoc) {
        Location movement = currentLoc.clone().subtract(prevLoc);
        if (section != Section.OUTSIDE) {
            //BotBowsManager.debugMessage(STR."\{PLAYER.getName()}s armorstands moved Δ(\{movement.getX()}, \{movement.getY()}, \{movement.getZ()})");
            AS_1.teleport(AS_1.getLocation().add(movement));
            AS_2.teleport(AS_2.getLocation().add(movement));
            if (TestCommand.rotation) {
                AS_1.setRotation(currentLoc.getYaw(), currentLoc.getPitch());
                AS_2.setRotation(currentLoc.getYaw(), currentLoc.getPitch());
            }
        }
    }

    private void updateArmorStandsAndSection() {
        if (section != Section.OUTSIDE) {
            /*if (enteredGreenArea()) {
                PLAYER.teleport(PLAYER.getLocation().add(4, 0, 11));
            } else if (enteredPurpleArea()) {
                PLAYER.teleport(PLAYER.getLocation().add(-4, 0, -11));
            }*/
            removeArmorStands();
        }

        if (isInGreenArea()) {
            section = Section.GREEN;
            AS_1 = spawnArmorStand(PLAYER.getLocation().add(-4, 0, -11));
            AS_2 = spawnArmorStand(PLAYER.getLocation().add(-8, 0, -22));
        } else if (isInPurpleArea()) {
            section = Section.PURPLE;
            AS_1 = spawnArmorStand(PLAYER.getLocation().add(4, 0, 11));
            AS_2 = spawnArmorStand(PLAYER.getLocation().add(-4, 0, -11));
        } else {
            section = Section.OUTSIDE;
        }
    }

    private ArmorStand spawnArmorStand(Location location) {
        //debugMessage(STR."Armor stand was spawned: \{BotBowsManager.getTeam(PLAYER)}");
        ArmorStand AS = Main.WORLD.spawn(location, ArmorStand.class);
        AS.setArms(true);
        AS.setBasePlate(false);
        AS.setGravity(false);
        AS.setInvulnerable(true);
        AS.setRightArmPose(new EulerAngle(275f,346f,0f));
        AS.setLeftArmPose(new EulerAngle(275f,49f,0f));
        AS.getEquipment().setItemInMainHand(BotBows.BOTBOW);
        AS.getEquipment().setArmorContents(new ItemStack[] {
                BOTBOWSPLAYER.getArmorPiece(Material.LEATHER_BOOTS),
                BOTBOWSPLAYER.getArmorPiece(Material.LEATHER_LEGGINGS),
                BOTBOWSPLAYER.getArmorPiece(Material.LEATHER_CHESTPLATE),
                BOTBOWSPLAYER.getArmorPiece(Material.LEATHER_HELMET)});
        return AS;
    }

    private void removeArmorStands() {
        if (AS_1 != null) AS_1.remove();
        if (AS_2 != null) AS_2.remove();
        AS_1 = null;
        AS_2 = null;
        //BotBowsManager.debugMessage("Armor stands removed");
    }

    private boolean isInGreenArea() {
        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), GREEN_BOUNDING_BOX_MIN, GREEN_BOUNDING_BOX_MAX);
    }

    private boolean isInPurpleArea() {
        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), PURPLE_BOUNDING_BOX_MIN, PURPLE_BOUNDING_BOX_MAX);
    }

    /*private boolean enteredGreenArea() {
        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), GREEN_ENTER_BB_MIN, GREEN_ENTER_BB_MAX);
    }

    private boolean enteredPurpleArea() {
        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), PURPLE_ENTER_BB_MIN, PURPLE_ENTER_BB_MAX);
    }*/

    private boolean isInSameSection() {
        return switch (section) {
            case GREEN -> isInGreenArea();
            case PURPLE -> isInPurpleArea();
            case OUTSIDE -> !isInGreenArea() && !isInPurpleArea();
        };
    }

    private enum Section {
        GREEN, PURPLE, OUTSIDE
    }

    public String getSection() {return section.toString();}
}
