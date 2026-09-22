package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.model.stat.ResultDisplay
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar
import gruvexp.bbminigames.twtClassic.botbowsGames.BotBowsGame
import gruvexp.bbminigames.twtClassic.botbowsGames.IcyRavineGame
import gruvexp.bbminigames.twtClassic.botbowsGames.SpaceStationGame
import gruvexp.bbminigames.twtClassic.botbowsGames.SteamPunkGame
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import kotlin.math.max

class Lobby(val id: Int) {
    private val players = mutableMapOf<UUID, BotBowsPlayer>()
    private val spectators = mutableSetOf<Player>()
    var settings: Settings
    var botBowsGame: BotBowsGame? = null
    val isGameActive: Boolean  // hvis spillet har starta, så kan man ikke gjøre ting som /settings
        get() = botBowsGame != null

    var resultDisplay: ResultDisplay? = null

    init {
        BotBows.lobbyMenu.updateLobbyItem(this)
        settings = Settings(this)
        settings.initMenus()
    }

    fun joinGame(p: Player) {
        if (isGameActive) {
            p.sendMessage(Component.text("A game is already ongoing, wait until it ends before you join", NamedTextColor.RED))
            p.sendMessage(Component.text("If you want to, you can spectate the game by right clicking the lobby"))
            return
        }
        BotBows.getLobby(p)?.let { lobby ->
            if (lobby == this) {
                p.sendMessage(Component.text("You already joined!", NamedTextColor.RED))
                return@joinGame
            }
            lobby.leaveGame(p)
        }
        BotBows.getSpectatingLobby(p)?.removeSpectator(p)

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

    fun reconnect(bp: BotBowsPlayer, p: Player) {
        if (!isGameActive) return

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
        players.values.forEach { it.reset() }
        players.values
            .filter { it.avatar is PlayerAvatar }
            .forEach { it.setReady(false, 4) }
        spectators.toSet().forEach { removeSpectator(it) }

        botBowsGame = null
        BotBows.lobbyMenu.updateLobbyItem(this)
    }

    fun messagePlayers(message: Component) {
        settings.getPlayers().forEach { it.avatar.message(message) }
        spectators.forEach { it.sendMessage(message) }
    }

    fun titlePlayers(component: Component, seconds: Long) {
        val title = Title.title(
            component, Component.empty(),
            Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(seconds), Duration.ofMillis(250))
        )
        players.values.forEach { it.avatar.showTitle(title) }
        spectators.forEach { it.showTitle(title) }
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

    fun addSpectator(p: Player) {
        BotBows.getLobby(p)?.let {
            p.sendMessage(Component.text("You cant spectate a game when you are in a lobby", NamedTextColor.YELLOW))
            return@addSpectator
        }
        val currentSpectatingLobby = BotBows.getSpectatingLobby(p)
        if (currentSpectatingLobby == this) {
            p.sendMessage(Component.text("You are already spectating this lobby", NamedTextColor.YELLOW))
            return
        }
        BotBows.getSpectatingLobby(p)?.removeSpectator(p)

        spectators.add(p)
        p.gameMode = GameMode.SPECTATOR
        p.teleport(settings.mapSettings.currentMap.viewingLocation)
        botBowsGame?.botBowsBoard?.addViewer(p)
        BotBows.registerSpectatorLobby(p, this)
        p.sendMessage(Component.text("You are now spectating ")
            .append(Component.text("Lobby $id", NamedTextColor.GREEN))
        )
        players.values.forEach {
            it.avatar.message(p.displayName()
                .append(Component.text(" is now spectating the game", NamedTextColor.YELLOW))
            )
        }
        p.sendMessage(Component.text("To stop spectating, run ")
            .append(Component.text("/botbows stop_spectating_game", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/botbows stop_spectating_game"))
            )
        )
    }

    fun removeSpectator(p: Player) {
        if (BotBows.getSpectatingLobby(p) != this) return

        spectators.remove(p)
        p.gameMode = GameMode.ADVENTURE
        p.teleport(BotBows.GLOBAL_LOBBY_LOCATION)
        p.scoreboard = Bukkit.getScoreboardManager().newScoreboard
        BotBows.unRegisterSpectatorLobby(p)
        p.sendMessage(Component.text("You are lo longer spectating ")
            .append(Component.text("Lobby $id", NamedTextColor.GREEN))
        )
        players.values.forEach {
            it.avatar.message(p.displayName()
                .append(Component.text(" is no longer spectating the game", NamedTextColor.YELLOW))
            )
        }
    }

    fun cleanupResultDisplay() {
        resultDisplay?.remove()
        resultDisplay = null
    }

    companion object {
        val READY = Menu.makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Ready", NamedTextColor.GREEN),
            Component.text("When everyone else is also ready, the match will start"),
            Component.text("To unready, right click this item")
        )

        val NOT_READY = Menu.makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Not Ready", NamedTextColor.RED),
            Component.text("The match will not start until youre ready"),
            Component.text("To ready up, right click this item")
        )

        val LOADING = Menu.makeItem(
            Material.YELLOW_STAINED_GLASS_PANE, Component.text("Loading...", NamedTextColor.YELLOW),
            Component.text("Please wait for your action to be processed")
        )

        private var botId = 0
    }
}
