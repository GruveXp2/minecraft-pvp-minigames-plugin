package gruvexp.bbminigames.twtClassic.team

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import java.util.function.Consumer
import kotlin.math.min

enum class BotBowsTeam(
    val displayName: String,
    val color: NamedTextColor,
    val dyeColor: DyeColor,
    val teamSide: TeamSide?,
    val spawnPos: Array<Location>,
    val tribunePos: Location
) {
    // --- CLASSIC ARENA ---
    BLAUD(
        "Blaud", NamedTextColor.BLUE, DyeColor.BLUE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD, -215.5, 22.0, -167.5, 90f, 10f),
            Location(Main.WORLD, -215.5, 22.0, -164.5, 90f, 10f),
            Location(Main.WORLD, -213.5, 22.0, -169.5, 90f, 10f),
            Location(Main.WORLD, -213.5, 22.0, -162.5, 90f, 10f),
            Location(Main.WORLD, -212.5, 22.0, -166.0, 90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -123.5, 180f, 10f)
    ),

    SAUCE(
        "Sauce", NamedTextColor.RED, DyeColor.RED, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD, -268.5, 22.0, -164.5, -90f, 10f),
            Location(Main.WORLD, -268.5, 22.0, -167.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -162.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -169.5, -90f, 10f),
            Location(Main.WORLD, -271.5, 22.0, -166.0, -90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -208.5, 0f, 10f)
    ),

    // --- ICY RAVINE ---
    GRAUT(
        "Graut", NamedTextColor.LIGHT_PURPLE, DyeColor.PURPLE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD, -268.5, 22.0, -274.5, -90f, 10f),
            Location(Main.WORLD, -268.5, 22.0, -277.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -272.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -279.5, -90f, 10f),
            Location(Main.WORLD, -271.5, 22.0, -276.0, -90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -318.5, 0f, 10f)
    ),

    WACKY(
        "Wacky", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD, -215.5, 22.0, -277.5, 90f, 10f),
            Location(Main.WORLD, -215.5, 22.0, -274.5, 90f, 10f),
            Location(Main.WORLD, -213.5, 22.0, -279.5, 90f, 10f),
            Location(Main.WORLD, -213.5, 22.0, -272.5, 90f, 10f),
            Location(Main.WORLD, -212.5, 22.0, -276.0, 90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -233.5, 180f, 10f)
    ),

    // --- ROYAL CASTLE ---
    KJØDD(
        "Kjødd", NamedTextColor.DARK_AQUA, DyeColor.ORANGE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD, -211.5, 22.0, -377.5, 90f, 10f),
            Location(Main.WORLD, -211.5, 22.0, -374.5, 90f, 10f),
            Location(Main.WORLD, -212.5, 22.0, -379.5, 90f, 10f),
            Location(Main.WORLD, -212.5, 22.0, -372.5, 90f, 10f),
            Location(Main.WORLD, -210.3, 22.0, -376.0, 90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -334.5, 180f, 10f)
    ),

    GOOFY(
        "Goofy", NamedTextColor.DARK_GREEN, DyeColor.GREEN, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD, -268.5, 22.0, -377.5, -90f, 10f),
            Location(Main.WORLD, -268.5, 22.0, -374.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -379.5, -90f, 10f),
            Location(Main.WORLD, -270.5, 22.0, -372.5, -90f, 10f),
            Location(Main.WORLD, -271.3, 22.0, -376.0, -90f, 10f)
        ),
        Location(Main.WORLD, -242.0, 26.0, -417.5, 0f, 10f)
    ),

    // --- STEAMPUNK ---
    BLOCC(
        "Blocc", NamedTextColor.GOLD, DyeColor.ORANGE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD, -327.5, 34.0, -377.5, 90f, 10f),
            Location(Main.WORLD, -327.5, 34.0, -374.5, 90f, 10f),
            Location(Main.WORLD, -325.5, 34.0, -379.5, 90f, 10f),
            Location(Main.WORLD, -325.5, 34.0, -372.5, 90f, 10f),
            Location(Main.WORLD, -324.5, 34.0, -376.0, 90f, 10f)
        ),
        Location(Main.WORLD, -357.0, 26.0, -333.3, 180f, 20f)
    ),

    QUICC(
        "Quicc", NamedTextColor.AQUA, DyeColor.CYAN, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD, -385.5, 34.0, -377.5, -90f, 10f),
            Location(Main.WORLD, -385.5, 34.0, -374.5, -90f, 10f),
            Location(Main.WORLD, -387.5, 34.0, -379.5, -90f, 10f),
            Location(Main.WORLD, -387.5, 34.0, -372.5, -90f, 10f),
            Location(Main.WORLD, -388.5, 34.0, -376.0, -90f, 10f)
        ),
        Location(Main.WORLD, -357.0, 26.0, -418.5, 0f, 20f)
    ),

    // --- PIGLIN HIDEOUT ---
    PIGLIN(
        "Piglin", NamedTextColor.GOLD, DyeColor.ORANGE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD, -382.5, 22.0, -164.5, -90f, 10f),
            Location(Main.WORLD, -382.5, 22.0, -167.5, -90f, 10f),
            Location(Main.WORLD, -380.5, 22.0, -162.5, -90f, 10f),
            Location(Main.WORLD, -380.5, 22.0, -169.5, -90f, 10f),
            Location(Main.WORLD, -385.5, 22.0, -166.0, -90f, 10f)
        ),
        Location(Main.WORLD, -368.5, 41.0, -166.0, 0f, 20f)
    ),

    HOGLIN(
        "Hoglin", NamedTextColor.YELLOW, DyeColor.BROWN, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD, -329.5, 22.0, -164.5, -90f, 10f),
            Location(Main.WORLD, -329.5, 22.0, -167.5, -90f, 10f),
            Location(Main.WORLD, -327.5, 22.0, -162.5, -90f, 10f),
            Location(Main.WORLD, -327.5, 22.0, -169.5, -90f, 10f),
            Location(Main.WORLD, -326.5, 22.0, -166.0, -90f, 10f)
        ),
        Location(Main.WORLD, -343.5, 41.0, -166.0, 0f, 20f)
    ),

    // --- INSIDE BOTBASE ---
    CORNER(
        "Corner", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
        Location(Main.WORLD, -58.5, 30.0, -212.5, 180f, -10f),
        Location(Main.WORLD, -29.5, 27.0, -211.0, 180f, 10f)
    ),

    CORE_INSIDE(
        "Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_2,
        Location(Main.WORLD, -6.5, 6.0, -264.5, 45f, 30f),
        Location(Main.WORLD, -29.5, 27.0, -273.0, 0f, -10f)
    ),

    // --- OUTSIDE BOTBASE ---
    CORE_OUTSIDE(
        "Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_1,
        Location(Main.WORLD, -75.5, 26.0, -259.5, 45f, -20f),
        Location(Main.WORLD, -67.5, 24.5, -267.5, -315f, 15f)
    ),

    MOUNTAIN(
        "Mountain", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, TeamSide.TEAM_2,
        Location(Main.WORLD, -106.0, 15.5, -205.0, 180f, 0f),
        Location(Main.WORLD, -109.5, 28.0, -220.5, -150f, 15f)
    ),

    // --- ROCKET FOREST ---
    DOOR(
        "Door", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
        Location(Main.WORLD, -75.0, 4.0, -201.5, 0f, 10f),
        Location(Main.WORLD, -70.5, 15.0, -197.0, -25f, 33f)
    ),

    TUNNEL(
        "Tunnel", NamedTextColor.DARK_GREEN, DyeColor.GREEN, TeamSide.TEAM_2,
        Location(Main.WORLD, -34.0, 11.5, -197.0, 33f, 0f),
        Location(Main.WORLD, -18.5, 29.0, -193.5, 55f, 15f)
    ),

    // --- ROCKET ---
    DROPPER(
        "Dropper", NamedTextColor.BLACK, DyeColor.BLACK, TeamSide.TEAM_1,
        Location(Main.WORLD, 4.5, 74.0, 18.5, 0f, 60f),
        Location(Main.WORLD, 1.5, 58.0, 20.5, -40f, 17f)
    ),

    ENGINE(
        "Engine", NamedTextColor.RED, DyeColor.RED, TeamSide.TEAM_2,
        Location(Main.WORLD, 2.5, 47.0, 36.5, 135f, 10f),
        Location(Main.WORLD, -5.5, 50.0, 39.5, -130f, 15f)
    ),

    // --- SPACE STATION ---
    WARM(
        "Warm", NamedTextColor.GOLD, DyeColor.ORANGE, TeamSide.TEAM_1,
        arrayOf(
            Location(Main.WORLD_END, 123.5, 75.0, 225.5, 170f, 5f),
            Location(Main.WORLD_END, 186.5, 88.0, 200.5, 85f, 10f),
            Location(Main.WORLD_END, 117.5, 80.0, 211.5, 153.5f, 35f),
            Location(Main.WORLD_END, 186.5, 88.0, 202.5, 95f, 10f),
            Location(Main.WORLD_END, 116.5, 75.0, 226.5, 170f, 5f)
        ),
        Location(Main.WORLD_END, 173.5, 89.5, 201.5, 90f, 10f)
    ),

    COLD(
        "Cold", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, TeamSide.TEAM_2,
        arrayOf(
            Location(Main.WORLD_END, 128.5, 88.0, 267.5, 180f, 10f),
            Location(Main.WORLD_END, 165.5, 74.5, 173.5, 90f, -5f),
            Location(Main.WORLD_END, 126.5, 90.0, 264.5, -163.5f, 30f),
            Location(Main.WORLD_END, 169.5, 22.0, 173.5, 90f, 10f),
            Location(Main.WORLD_END, 130.5, 89.0, 262.0, 150f, 35f)
        ),
        Location(Main.WORLD_END, 149.5, 87.0, 168.5, 0f, 10f)
    );

    constructor(
        displayName: String, color: NamedTextColor, dyeColor: DyeColor, teamSide: TeamSide,
        spawnPos: Location, tribunePos: Location
    ) : this(displayName, color, dyeColor, teamSide, Array(5) { spawnPos }, tribunePos)

    var oppositeTeam: BotBowsTeam? = null
        set(oppositeTeam) {
            check(field == null) { "This team already has an assigned opposite team" }
            field = oppositeTeam
        }
    val players: MutableList<BotBowsPlayer> = ArrayList(4)

    fun clearPlayers() {
        players.clear() // TODO: sett availiable = true (available bestemmer om mappet er ledig eller ikke)
    }

    fun putPlayers(newPlayers: Collection<BotBowsPlayer>) {
        players.clear()
        players.addAll(newPlayers)
        for (bp in players) {
            bp.updateTeam(this)
            bp.teleport(tribunePos)
        }
    }

    var points: Int = 0
        private set

    fun tpPlayersToSpawn() {
        for (i in players.indices) {
            players[i].teleport(spawnPos[min(i, 4)])
        }
    }

    fun join(bp: BotBowsPlayer) {
        players.add(bp)
        bp.teleport(tribunePos)
        bp.onTeamJoin(this)
    }

    fun leave(bp: BotBowsPlayer) {
        players.remove(bp)
        bp.onTeamLeave()
    }

    fun reset() {
        points = 0
    }

    fun size(): Int {
        return players.size
    }

    fun hasPlayer(bp: BotBowsPlayer): Boolean {
        return players.contains(bp)
    }

    fun getPlayer(id: Int): BotBowsPlayer {
        return players[id]
    }

    fun getPlayerID(bp: BotBowsPlayer?): Int {
        return players.indexOf(bp)
    }

    val isEmpty: Boolean
        get() = players.isEmpty()

    fun getSpawnPos(bp: BotBowsPlayer?): Location {
        return spawnPos[players.indexOf(bp)]
    }

    fun addPoints(score: Int) {
        points += score
    }

    val healthPercentage: Int
        get() {
            var totalHealth = 0
            var currentHealth = 0
            for (bp in players) {
                totalHealth += bp.settings.maxHealth
                currentHealth += bp.hp
            }
            val healthLevel = (currentHealth.toFloat() / totalHealth).toDouble()
            return (healthLevel * 100).toInt()
        }

    val isEliminated: Boolean
        get() {
            for (bp in players) {
                if (bp.hp > 0) return false
            }
            return true
        }

    fun glow(seconds: Int) {
        players.forEach(Consumer { bp: BotBowsPlayer? -> bp!!.avatar.setGlowing(true) })
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { players.forEach(Consumer { bp: BotBowsPlayer? -> bp!!.avatar.setGlowing(false) }) },
            20L * seconds
        )
    }

    fun setGlowColor(color: NamedTextColor?, ticks: Int) {
        players.forEach(Consumer { bp: BotBowsPlayer? -> bp!!.avatar.setColor(color) })
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { players.forEach(Consumer { bp: BotBowsPlayer? -> bp!!.avatar.setColor(this.color as NamedTextColor?) }) },
            ticks.toLong()
        )
    }

    val glassPane: Material
        get() = Material.getMaterial(dyeColor.name + "_STAINED_GLASS_PANE") ?: Material.GLASS_PANE

    fun toComponent(): TextComponent {
        return Component.text(displayName, color)
    }
}
