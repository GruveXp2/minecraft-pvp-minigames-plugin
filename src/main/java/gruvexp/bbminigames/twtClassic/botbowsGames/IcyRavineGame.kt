package gruvexp.bbminigames.twtClassic.botbowsGames

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle

class IcyRavineGame(settings: Settings) : BotBowsGame(settings) {
    override fun leaveGame(bp: BotBowsPlayer) {
        super.leaveGame(bp)
        dungeonScanners.remove(bp)?.cancel()
        dungeonGhosters.remove(bp)
    }

    override fun startGame() {
        super.startGame()
        initDungeon()
    }

    override fun startRound() {
        super.startRound()
        startScanners()
    }

    override fun handleMovement(e: PlayerMoveEvent) {
        super.handleMovement(e)
        val playerId = e.player.uniqueId
        val bp = lobby.getBotBowsPlayer(playerId) ?: return
        if (settings.isPlayerJoined(playerId) && isInDungeon(bp)) {
            handleDungeonMovement(bp)
        }
    }

    override fun postGame(winningTeam: BotBowsTeam?) {
        super.postGame(winningTeam)
        dungeonScanners.values.forEach { it.cancel() }
        dungeonScanners.clear()
        dungeonGhosters.clear()
    }

    private fun initDungeon() {
        players.forEach { dungeonGhosters[it] = DungeonGhoster(it) }
    }

    private fun startScanners() {
        players.forEach {
            val scanner = DungeonProximityScanner(it)
            dungeonScanners[it] = scanner
            scanner.runTaskTimer(Main.getPlugin(), 140L, 5L)
        }
    }

    fun isInDungeon(bp: BotBowsPlayer): Boolean {
        return dungeonScanners[bp]!!.isInDungeon
    }

    fun getSectionId(bp: BotBowsPlayer): String {
        return dungeonGhosters[bp]!!.sectionId
    }

    fun handleDungeonMovement(bp: BotBowsPlayer) {
        dungeonGhosters[bp]!!.handleMovement()
    }


    private class DungeonProximityScanner(val bp: BotBowsPlayer) : BukkitRunnable() {
        var isInDungeon: Boolean = false
            private set

        override fun run() { // checks if a player is near the dungeon, doesn't scan that often to not waste resources
            if (isInDungeon != isInsideBoundingBox(bp.location, BOUNDING_BOX_MIN, BOUNDING_BOX_MAX)) {
                //BotBowsManager.debugMessage(STR."\{p.getName()} moved thru the bounding box");
            }
            isInDungeon = isInsideBoundingBox(bp.location, BOUNDING_BOX_MIN, BOUNDING_BOX_MAX)
        }

        companion object {
            // 9.11.2024
            private val BOUNDING_BOX_MIN = Location(Main.WORLD, -259.0, 12.0, -304.0)
            private val BOUNDING_BOX_MAX = Location(Main.WORLD, -240.0, 19.0, -283.0)
            fun isInsideBoundingBox(loc: Location, min: Location, max: Location): Boolean {
                return loc.x >= min.x && loc.x < max.x && loc.y >= min.y && loc.y < max.y && loc.z >= min.z && loc.z < max.z
            }
        }
    }

    private class DungeonGhoster(private val bp: BotBowsPlayer) {
        private var section = Section.OUTSIDE
        private var as1: ArmorStand? = null
        private var as2: ArmorStand? = null

        private var prevLoc: Location = bp.location

        init {
            updateArmorStandsAndSection()
        }

        fun handleMovement() {
            val currentLoc = bp.location
            if (currentLoc == prevLoc) {
                Main.getPlugin().logger.info("Somehow handleMovement() was called when the player didnt move (bug)")
                return  // no movement, no need to process
            }

            if (isInSameSection) {
                updateArmorStandsPosition(currentLoc)
            } else {
                //String oldSection = section.toString();
                updateArmorStandsAndSection()
                //debugMessage(STR."\{PLAYER.getName()} moved: \{ChatColor.WHITE}\{oldSection} -> \{section.toString()}");
            }
            prevLoc = currentLoc
        }

        fun updateArmorStandsPosition(currentLoc: Location) {
            val movement = currentLoc.clone().subtract(prevLoc)
            if (section != Section.OUTSIDE) {
                //BotBowsManager.debugMessage(STR."\{PLAYER.getName()}s armorstands moved Δ(\{movement.getX()}, \{movement.getY()}, \{movement.getZ()})");
                as1!!.teleport(as1!!.location.add(movement))
                as2!!.teleport(as2!!.location.add(movement))
                if (TestCommand.rotation) {
                    as1!!.setRotation(currentLoc.yaw, currentLoc.pitch)
                    as2!!.setRotation(currentLoc.yaw, currentLoc.pitch)
                }
            }
        }

        fun updateArmorStandsAndSection() {
            if (section != Section.OUTSIDE) {
                /*if (enteredGreenArea()) {
                PLAYER.teleport(PLAYER.getLocation().add(4, 0, 11));
            } else if (enteredPurpleArea()) {
                PLAYER.teleport(PLAYER.getLocation().add(-4, 0, -11));
            }*/
                removeArmorStands()
            }

            if (this.isInGreenArea) {
                section = Section.GREEN
                as1 = spawnArmorStand(bp.location.add(-4.0, 0.0, -11.0))
                as2 = spawnArmorStand(bp.location.add(-8.0, 0.0, -22.0))
            } else if (isInPurpleArea) {
                section = Section.PURPLE
                as1 = spawnArmorStand(bp.location.add(4.0, 0.0, 11.0))
                as2 = spawnArmorStand(bp.location.add(-4.0, 0.0, -11.0))
            } else {
                section = Section.OUTSIDE
            }
        }

        fun spawnArmorStand(location: Location): ArmorStand {
            //debugMessage(STR."Armor stand was spawned: \{BotBowsManager.getTeam(PLAYER)}");
            return location.world.spawn(location, ArmorStand::class.java).apply {
                setArms(true)
                setBasePlate(false)
                setGravity(false)
                isInvulnerable = true
                rightArmPose = EulerAngle(275.0, 346.0, 0.0)
                leftArmPose = EulerAngle(275.0, 49.0, 0.0)
                equipment.setItemInMainHand(BotBows.BOTBOW)
                val armor = bp.avatar.armor
                equipment.armorContents = arrayOf(armor.boots, armor.leggings, armor.chestplate, armor.helmet)
            }
        }

        fun removeArmorStands() {
            as1?.remove()
            as2?.remove()
            as1 = null
            as2 = null
            //BotBowsManager.debugMessage("Armor stands removed");
        }

        val isInGreenArea: Boolean
            get() = DungeonProximityScanner.isInsideBoundingBox(
                bp.location,
                GREEN_BOUNDING_BOX_MIN,
                GREEN_BOUNDING_BOX_MAX
            )

        val isInPurpleArea: Boolean
            get() = DungeonProximityScanner.isInsideBoundingBox(
                bp.location,
                PURPLE_BOUNDING_BOX_MIN,
                PURPLE_BOUNDING_BOX_MAX
            )

        val isInSameSection: Boolean
            /*private boolean enteredGreenArea() {
                        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), GREEN_ENTER_BB_MIN, GREEN_ENTER_BB_MAX);
                    }
            
                    private boolean enteredPurpleArea() {
                        return GvwDungeonProximityScanner.isInsideBoundingBox(PLAYER.getLocation(), PURPLE_ENTER_BB_MIN, PURPLE_ENTER_BB_MAX);
                    }*/
            get() =// 9.11.2024
                when (section) {
                    Section.GREEN -> isInGreenArea
                    Section.PURPLE -> isInPurpleArea
                    Section.OUTSIDE -> !isInGreenArea && !isInPurpleArea
                }

        private enum class Section {
            GREEN, PURPLE, OUTSIDE
        }

        val sectionId: String
            get() = "$section"

        companion object {
            // 9.11.2024
            private val GREEN_BOUNDING_BOX_MIN = Location(Main.WORLD, -250.0, 15.0, -297.0)
            private val GREEN_BOUNDING_BOX_MAX = Location(Main.WORLD, -245.0, 19.0, -287.0)
            private val PURPLE_BOUNDING_BOX_MIN = Location(Main.WORLD, -255.0, 16.0, -299.0)
            private val PURPLE_BOUNDING_BOX_MAX = Location(Main.WORLD, -251.0, 19.0, -288.0)

            private val PURPLE_ENTER_BB_MIN = Location(Main.WORLD, -250.0, 16.0, -287.3)
            private val PURPLE_ENTER_BB_MAX = Location(Main.WORLD, -247.0, 18.0, -286.0)

            private val GREEN_ENTER_BB_MIN = Location(Main.WORLD, -254.0, 16.0, -300.0)
            private val GREEN_ENTER_BB_MAX = Location(Main.WORLD, -251.0, 18.0, -287.7)
        }
    }

    companion object {
        private val dungeonScanners = mutableMapOf<BotBowsPlayer, DungeonProximityScanner>()
        private val dungeonGhosters = mutableMapOf<BotBowsPlayer, DungeonGhoster>()
    }
}
