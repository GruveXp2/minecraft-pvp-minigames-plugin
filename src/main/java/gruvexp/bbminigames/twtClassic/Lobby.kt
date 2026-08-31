package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame
import gruvexp.bbminigames.twtClassic.botbowsGames.IcyRavineGame
import gruvexp.bbminigames.twtClassic.botbowsGames.SpaceStationGame
import gruvexp.bbminigames.twtClassic.botbowsGames.SteamPunkGame
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.util.*
import kotlin.math.max

class Lobby(val id: Int) {
    private val players = HashMap<UUID, BotBowsPlayer>()
    @JvmField
    var settings: Settings
    @JvmField
    var botBowsGame: BotBowsGame? = null
    val isGameActive: Boolean  // hvis spillet har starta, så kan man ikke gjøre ting som /settings
        get() = botBowsGame != null

    init {
        BotBows.lobbyMenu.updateLobbyItem(this)
        settings = Settings(this)
        settings.initMenus()
    }


    fun joinGame(p: Player) {
        if (isGameActive) {
            p.sendMessage(Component.text("A game is already ongoing, wait until it ends before you join", NamedTextColor.RED))
            return
        }
        if (BotBows.getLobby(p) != null) {
            if (BotBows.getLobby(p) == this) {
                p.sendMessage(Component.text("You already joined!", NamedTextColor.RED))
                return
            }
            BotBows.getLobby(p).leaveGame(p)
        }
        p.inventory.clear()
        settings.joinGame(p)
        BotBows.lobbyMenu.updateLobbyItem(this)
        BotBows.registerPlayerLobby(p.uniqueId, this)
        p.inventory.setItem(0, BotBows.SETTINGS_ITEM)
        p.inventory.setItem(4, NOT_READY)
    }

    fun addBot(): UUID {
        val mannequin = Main.WORLD.spawn(Main.WORLD.spawnLocation, Mannequin::class.java)
        mannequin.customName(Component.text("BotBowBot ${botId++}"))
        mannequin.profile = (ResolvableProfile.resolvableProfile(Bukkit.createProfile(UUID.fromString("b62d350f-6b7e-41c3-9dda-8404730245ef"))))
        settings.joinGame(mannequin)
        BotBows.lobbyMenu.updateLobbyItem(this)
        val id = mannequin.uniqueId
        BotBows.registerPlayerLobby(id, this)
        return id
    }

    fun leaveGame(bp: BotBowsPlayer) {
        if (isGameActive) {
            botBowsGame!!.leaveGame(bp)
        } else {
            settings.leaveGame(bp)
        }
        players.remove(bp.avatar.uuid)
        BotBows.lobbyMenu.updateLobbyItem(this)
        BotBows.unRegisterPlayerLobby(bp.avatar.uuid)
    }

    fun disconnect(bp: BotBowsPlayer) {
        if (!isGameActive) {
            leaveGame(bp)
            return
        }
        messagePlayers(bp.name.append(Component.text(" disconnected from the server and will be replaced by a bot", NamedTextColor.YELLOW)))
        val id = bp.turnIntoBot()
        BotBows.registerPlayerLobby(id, this)
        registerBotBowsPlayerAvatar(bp)
    }

    fun reconnect(p: Player) {
        if (!isGameActive) return

        val bp = BotBows.getBotBowsPlayer(p)
        messagePlayers(bp.name.append(Component.text(" reconnected to the game", NamedTextColor.GREEN)))
        bp.turnIntoPlayer(p)
    }

    fun leaveGame(p: Player) {
        val playerId = p.uniqueId
        val bp: BotBowsPlayer = getBotBowsPlayer(playerId) ?: run {
            p.sendMessage("Nothing happened, you werent in the game in the first place")
            return@leaveGame
        }
        leaveGame(bp)
    }

    fun replacePlayerId(oldId: UUID, newId: UUID) {
        val bp: BotBowsPlayer = players[oldId]!!
        players.remove(oldId)
        players[newId] = bp
    }

    fun getBotBowsPlayer(p: Player): BotBowsPlayer? {
        return getBotBowsPlayer(p.uniqueId)
    }

    fun getBotBowsPlayer(playerId: UUID): BotBowsPlayer? {
        return players[playerId]
    }

    fun registerBotBowsPlayerAvatar(bp: BotBowsPlayer) {
        if (players.containsKey(bp.avatar.uuid)) return
        players[bp.avatar.uuid] = bp
    }

    fun getPlayers(): MutableCollection<BotBowsPlayer> {
        return players.values
    }

    fun startGame(gameStarter: Player) {
        if (isGameActive) {
            gameStarter.sendMessage(Component.text("The game has already started!", NamedTextColor.RED))
            return
        } else if (settings.team1.isEmpty || settings.team2.isEmpty) {
            gameStarter.sendMessage(Component.text("Cant start game, both teams must have at least 1 player each", NamedTextColor.RED))
            return
        }
        messagePlayers(
            Component.text("${gameStarter.name}: ", NamedTextColor.GRAY)
                .append(Component.text("The game has started!", NamedTextColor.GREEN))
        )
        startGame()
    }

    private fun startGame() {
        val randomMap = settings.mapSettings.finalizeMapSelection()
        if (randomMap != null) {
            messagePlayers(
                Component.text("A random map was picked: ")
                    .append(Component.text(randomMap.prettyName(), NamedTextColor.GREEN))
            )
        }
        botBowsGame = when (settings.mapSettings.currentMap) {
            BotBowsMap.ICY_RAVINE -> IcyRavineGame(settings)
            BotBowsMap.STEAMPUNK -> SteamPunkGame(settings)
            BotBowsMap.SPACE_STATION -> SpaceStationGame(settings)
            else -> BotBowsGame(settings)
        }.apply { startGame() }
    }

    fun reset() {
        HashSet(players.keys).forEach { id ->
            players[id]!!.destroy()
            players.remove(id)
            BotBows.unRegisterPlayerLobby(id)
        }

        botBowsGame = null
        settings = Settings(this)
        settings.initMenus()
        BotBows.lobbyMenu.updateLobbyItem(this)
    }

    fun messagePlayers(message: Component) {
        settings.getPlayers().forEach { it.avatar.message(message) }
    }

    fun titlePlayers(component: Component, seconds: Long) {
        for (bp in players.values) {
            bp.avatar.showTitle(Title.title(
                component, Component.empty(),
                Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(seconds), Duration.ofMillis(250))
            ))
        }
    }

    fun check4Elimination(dedPlayer: BotBowsPlayer) {
        botBowsGame!!.check4Elimination(dedPlayer)
    }

    val totalPlayers: Int
        get() = players.size

    fun handlePlayerReady(bp: BotBowsPlayer) {
        val ready = bp.settings.isReady
        val readyPlayers = players.values.count { it.settings.isReady }
        val totalPlayers = max(players.size, 2)

        messagePlayers(
            Component.text("${bp.plainName} ${if (ready) "has readied up " else "is no longer ready" } ($readyPlayers/$totalPlayers)", NamedTextColor.YELLOW)
        )
        if (readyPlayers == totalPlayers && !(settings.team1.isEmpty || settings.team2.isEmpty)) {
            messagePlayers(Component.text("Everybody are ready, starting game in 5 seconds", NamedTextColor.GREEN))
            settings.finishMapSelection()
            startGame()
        }
    }

    companion object {
        @JvmField
        val READY: ItemStack = Menu.makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Ready", NamedTextColor.GREEN),
            Component.text("When everyone else is also ready, the match will start"),
            Component.text("To unready, right click this item")
        )

        @JvmField
        val NOT_READY: ItemStack = Menu.makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Not Ready", NamedTextColor.RED),
            Component.text("The match will not start until youre ready"),
            Component.text("To ready up, right click this item")
        )

        val LOADING: ItemStack = Menu.makeItem(
            Material.YELLOW_STAINED_GLASS_PANE, Component.text("Loading...", NamedTextColor.YELLOW),
            Component.text("Please wait for your action to be processed")
        )

        private var botId = 0
    }
}
