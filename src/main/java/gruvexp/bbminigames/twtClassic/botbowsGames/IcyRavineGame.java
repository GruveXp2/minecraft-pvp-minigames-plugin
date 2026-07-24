package gruvexp.bbminigames.twtClassic.botbowsGames;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar;
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IcyRavineGame extends BotBowsGame {

    private static final Map<BotBowsPlayer, DungeonProximityScanner> dungeonScanners = new HashMap<>();
    private static final Map<BotBowsPlayer, DungeonGhoster> dungeonGhosters = new HashMap<>();

    public IcyRavineGame(Settings settings) {
        super(settings);
    }

    @Override
    public void leaveGame(BotBowsPlayer bp) {
        super.leaveGame(bp);
        if (dungeonScanners.containsKey(bp)) {
            dungeonScanners.get(bp).cancel();
            dungeonScanners.remove(bp);
            dungeonGhosters.remove(bp);
        }
    }

    @Override
    public void startGame() {
        super.startGame();
        initDungeon();
    }

    @Override
    public void startRound() {
        super.startRound();
        startScanners();
    }

    @Override
    public void handleMovement(PlayerMoveEvent e) {
        super.handleMovement(e);
        UUID playerId = e.getPlayer().getUniqueId();
        BotBowsPlayer bp = lobby.getBotBowsPlayer(playerId);
        if (settings.isPlayerJoined(playerId) && isInDungeon(bp)) {
            handleDungeonMovement(bp);
        }
    }

    @Override
    public void postGame(BotBowsTeam winningTeam) {
        super.postGame(winningTeam);
        for (BukkitRunnable scanner : dungeonScanners.values()) {
            scanner.cancel();
        }
        dungeonScanners.clear();
        dungeonGhosters.clear();
    }

    private void initDungeon() {
        for (BotBowsPlayer bp : players) {
            dungeonGhosters.put(bp, new DungeonGhoster(bp));
        }
    }

    private void startScanners() {
        for (BotBowsPlayer bp : players) {
            DungeonProximityScanner scanner = new DungeonProximityScanner(bp);
            dungeonScanners.put(bp, scanner);
            scanner.runTaskTimer(Main.getPlugin(), 140L, 5L);
        }
        //debugMessage("starting scanners");
    }

    public boolean isInDungeon(BotBowsPlayer bp) {
        return dungeonScanners.get(bp).isInDungeon();
    }

    public String getSection(BotBowsPlayer bp) {
        return dungeonGhosters.get(bp).getSection();
    }

    public void handleDungeonMovement(BotBowsPlayer bp) {
        dungeonGhosters.get(bp).handleMovement();
    }


    private static class DungeonProximityScanner extends BukkitRunnable { // 9.11.2024

        private static final Location BOUNDING_BOX_MIN = new Location(Main.WORLD, -259, 12, -304);
        private static final Location BOUNDING_BOX_MAX = new Location(Main.WORLD, -240, 19, -283);
        public final BotBowsPlayer bp;
        private boolean isInDungeon = false;

        public DungeonProximityScanner(BotBowsPlayer bp) {
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

    private static class DungeonGhoster { // 9.11.2024

        private static final Location GREEN_BOUNDING_BOX_MIN = new Location(Main.WORLD, -250, 15, -297);
        private static final Location GREEN_BOUNDING_BOX_MAX = new Location(Main.WORLD, -245, 19, -287);
        private static final Location PURPLE_BOUNDING_BOX_MIN = new Location(Main.WORLD, -255, 16, -299);
        private static final Location PURPLE_BOUNDING_BOX_MAX = new Location(Main.WORLD, -251, 19, -288);

        private static final Location PURPLE_ENTER_BB_MIN = new Location(Main.WORLD, -250, 16, -287.3);
        private static final Location PURPLE_ENTER_BB_MAX = new Location(Main.WORLD, -247, 18, -286);

        private static final Location GREEN_ENTER_BB_MIN = new Location(Main.WORLD, -254, 16, -300);
        private static final Location GREEN_ENTER_BB_MAX = new Location(Main.WORLD, -251, 18, -287.7);

        private Section section = Section.OUTSIDE;
        private ArmorStand as1;
        private ArmorStand as2;

        private final BotBowsPlayer bp;
        private Location prevLoc;

        public DungeonGhoster(BotBowsPlayer bp) {
            this.bp = bp;
            prevLoc = bp.getLocation();
            updateArmorStandsAndSection();
        }

        public void handleMovement() {
            Location currentLoc = bp.getLocation();
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
                as1.teleport(as1.getLocation().add(movement));
                as2.teleport(as2.getLocation().add(movement));
                if (TestCommand.rotation) {
                    as1.setRotation(currentLoc.getYaw(), currentLoc.getPitch());
                    as2.setRotation(currentLoc.getYaw(), currentLoc.getPitch());
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
                as1 = spawnArmorStand(bp.getLocation().add(-4, 0, -11));
                as2 = spawnArmorStand(bp.getLocation().add(-8, 0, -22));
            } else if (isInPurpleArea()) {
                section = Section.PURPLE;
                as1 = spawnArmorStand(bp.getLocation().add(4, 0, 11));
                as2 = spawnArmorStand(bp.getLocation().add(-4, 0, -11));
            } else {
                section = Section.OUTSIDE;
            }
        }

        private ArmorStand spawnArmorStand(Location location) {
            //debugMessage(STR."Armor stand was spawned: \{BotBowsManager.getTeam(PLAYER)}");
            ArmorStand as = location.getWorld().spawn(location, ArmorStand.class);
            as.setArms(true);
            as.setBasePlate(false);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setRightArmPose(new EulerAngle(275f,346f,0f));
            as.setLeftArmPose(new EulerAngle(275f,49f,0f));
            as.getEquipment().setItemInMainHand(BotBows.BOTBOW);
            BotBowsAvatar.ArmorSet armor = bp.avatar.getArmor();
            as.getEquipment().setArmorContents(new ItemStack[] {armor.boots(), armor.leggings(), armor.chestplate(), armor.helmet()});
            return as;
        }

        private void removeArmorStands() {
            if (as1 != null) as1.remove();
            if (as2 != null) as2.remove();
            as1 = null;
            as2 = null;
            //BotBowsManager.debugMessage("Armor stands removed");
        }

        private boolean isInGreenArea() {
            return DungeonProximityScanner.isInsideBoundingBox(bp.getLocation(), GREEN_BOUNDING_BOX_MIN, GREEN_BOUNDING_BOX_MAX);
        }

        private boolean isInPurpleArea() {
            return DungeonProximityScanner.isInsideBoundingBox(bp.getLocation(), PURPLE_BOUNDING_BOX_MIN, PURPLE_BOUNDING_BOX_MAX);
        }

        /*private boolean enteredGreenArea() {
            return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), GREEN_ENTER_BB_MIN, GREEN_ENTER_BB_MAX);
        }

        private boolean enteredPurpleArea() {
            return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), PURPLE_ENTER_BB_MIN, PURPLE_ENTER_BB_MAX);
        }*/

        private boolean isInSameSection() { // 9.11.2024
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
}
