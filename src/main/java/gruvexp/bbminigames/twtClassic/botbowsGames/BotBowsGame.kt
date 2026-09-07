package gruvexp.bbminigames.twtClassic.botbowsGames

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.model.stat.MatchResult
import gruvexp.bbminigames.model.stat.ResultDisplay
import gruvexp.bbminigames.tasks.BotBowsGiver
import gruvexp.bbminigames.tasks.RoundCountdown
import gruvexp.bbminigames.tasks.RoundTimer
import gruvexp.bbminigames.twtClassic.*
import gruvexp.bbminigames.twtClassic.hazard.Hazard
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitTask
import java.time.Duration

open class BotBowsGame(val settings: Settings) {
    val lobby: Lobby = settings.lobby
    protected val team1: BotBowsTeam = settings.team1
    protected val team2: BotBowsTeam = settings.team2

    protected val players: Set<BotBowsPlayer> = settings.getPlayers()
    val botBowsBoard: BotBowsBoard = BotBowsBoard(lobby)
    protected val hazards: Collection<Hazard> = settings.hazardSettings.createActiveHazards()

    var canMove: Boolean = true
    var canInteract: Boolean = false // if youre able to shoot or use abilities
    var activeRound: Boolean = false // if the game is currently ongoing, this includes the countdown in the start of rounds
    protected var round: Int = 0
    private var roundTimer: BukkitTask? = null
    private var startRoundTask: BukkitTask? = null

    var matchResult: MatchResult = MatchResult(settings.mapSettings.currentMap)

    open fun leaveGame(bp: BotBowsPlayer) {
        val team = bp.team
        settings.leaveGame(bp)
        botBowsBoard.removePlayerScore(bp)
        if (team.isEmpty) Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { endGame() }, 10L)
    }

    open fun startGame() {
        if (TestCommand.debugging) {
            lobby.messagePlayers(Component.text("WARNING: test mode is on, match data wont be saved!", NamedTextColor.YELLOW))
            lobby.messagePlayers(
                Component.text("To turn off test mode, run ")
                    .append(Component.text("/test toggle_debugging", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.clickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            ClickEvent.Payload.string("/test toggle_debugging")
                        ))
                    )
            )
        }
        val teamManager = botBowsBoard.createBoard()
        startRound()
        hazards.forEach { it.init(players) }

        // legger til player liv osv
        players.forEach {
            it.initBattle(teamManager)
            botBowsBoard.updatePlayerScore(it)
        }
        botBowsBoard.initPlayers() // makes the player join the Team's to get the correct color outline
        botBowsBoard.updateTeamScores()
        players.forEach { it.start() }
        BotBowsGiver(lobby).runTaskTimer(Main.getPlugin(), 100L, 10L)
    }

    open fun startRound() {
        round++
        // alle har fullt med liv
        players.forEach {
            it.revive()
            it.readyAbilities()
        }
        team1.tpPlayersToSpawn()
        team2.tpPlayersToSpawn()
        canMove = false
        canInteract = false
        activeRound = true
        RoundCountdown(this, round).runTaskTimer(
            Main.getPlugin(),
            0L,
            20L
        ) // mens de er på spawn, kan de ikke bevege seg og det er nedtelling til det begynner
        val roundDuration = settings.winConditionSettings.roundDuration
        if (roundDuration != 0) {
            roundTimer = RoundTimer(this, roundDuration).runTaskTimer(Main.getPlugin(), 200L, 20L)
        }
    }

    fun triggerHazards() {
        hazards.forEach { it.triggerOnChance(players) }
    }

    val stormHazard: Hazard?
        get() = hazards.filterIsInstance<StormHazard>().firstOrNull()

    open fun handleMovement(e: PlayerMoveEvent) {
        BotBows.handleMovement(e)
    }

    fun check4Elimination(dedPlayer: BotBowsPlayer) {
        val losingTeam = dedPlayer.team

        if (losingTeam.isEliminated) {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { endRoundEliminated(losingTeam) }, 2L)
        }
    }

    private fun endRoundEliminated(losingTeam: BotBowsTeam) {
        val winningTeam = losingTeam.oppositeTeam
        if (winningTeam.isEliminated) { // last players on each team died ≈ at the same time
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW))
            postRound(null, 0)
            return
        }
        lobby.messagePlayers(
            winningTeam.toComponent()
                .append(Component.text(" won the round!", NamedTextColor.GREEN))
        )
        val isDynamicScoring = settings.winConditionSettings.isDynamicScoring
        val winScore = if (isDynamicScoring) calculateDynamicScore(winningTeam, losingTeam) else 1

        winningTeam.addPoints(winScore)
        Bukkit.getScheduler().runTaskLater(
            Main.getPlugin(),
            Runnable { postRound(winningTeam, winScore) },
            2L
        ) // 2 ticks delay i tilfelle alle dauer rett etterpå, da skal det bli draw isteden
    }

    fun endRoundTimeout() {
        val team1Percentage = team1.healthPercentage
        val team2Percentage = team2.healthPercentage
        var winningTeam: BotBowsTeam? = null
        val team1ResultColor: NamedTextColor
        val team2ResultColor: NamedTextColor
        if (team1Percentage > team2Percentage) {
            winningTeam = team1
            team1ResultColor = NamedTextColor.GREEN
            team2ResultColor = NamedTextColor.RED
        } else if (team1Percentage < team2Percentage) {
            winningTeam = team2
            team1ResultColor = NamedTextColor.RED
            team2ResultColor = NamedTextColor.GREEN
        } else {
            team2ResultColor = NamedTextColor.YELLOW
            team1ResultColor = team2ResultColor
        }
        lobby.messagePlayers(
            Component.empty()
                .append(Component.text("Round over!\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(team1.toComponent())
                .append(Component.text(": $team1Percentage% hp left\n", team1ResultColor))
                .append(team2.toComponent())
                .append(Component.text(": $team2Percentage% hp left\n", team2ResultColor))
        )
        if (winningTeam != null) {
            lobby.messagePlayers(
                winningTeam.toComponent()
                    .append(Component.text(" won the round!", NamedTextColor.GREEN))
            )
            val losingTeam = winningTeam.oppositeTeam
            val isDynamicScoring = settings.winConditionSettings.isDynamicScoring
            val winScore = if (isDynamicScoring) calculateDynamicScore(winningTeam, losingTeam) else 1

            winningTeam.addPoints(winScore)
            postRound(winningTeam, winScore)
        } else {
            lobby.messagePlayers(Component.text("The round ended in a tie!", NamedTextColor.YELLOW))
            postRound(null, 0)
        }
    }

    protected open fun postRound(winningTeam: BotBowsTeam?, winScore: Int) {
        if (!activeRound) return
        activeRound = false
        players.forEach {
            it.resetAbilities()
            it.clearEffects()
        }
        lobby.messagePlayers( // team1: %d points, team2: %d points
            team1.toComponent()
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(Component.text("${team1.points}\n", NamedTextColor.GREEN))
                .append(team2.toComponent())
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(Component.text(team2.points, NamedTextColor.GREEN))
        )
        roundTimer?.cancel()
        if (settings.rain > 0) {
            Main.WORLD.setStorm(false)
            Bukkit.getOnlinePlayers().forEach { it.resetPlayerWeather() }
        }
        hazards.forEach { if (it.isActive) it.end() }

        if (winningTeam == null) {
            lobby.titlePlayers(Component.text("DRAW", NamedTextColor.YELLOW), 2)
            canInteract = false
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { startRound() }, 40L)
            return
        }

        lobby.titlePlayers(Component.text("${winningTeam.displayName} +$winScore", winningTeam.color), 2)
        botBowsBoard.updateTeamScores()

        val winScoreThreshold = settings.winConditionSettings.winScoreThreshold
        if (winningTeam.points >= winScoreThreshold && winScoreThreshold > 0) {
            postGame(winningTeam)
        } else {
            canInteract = false
            startRoundTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { startRound() }, 40L)
        }
    }

    private fun calculateDynamicScore(winningTeam: BotBowsTeam, losingTeam: BotBowsTeam): Int {
        val hpLeft = winningTeam.players.sumOf { it.hp }
        lobby.messagePlayers(Component.text("${hpLeft}p for remaining hp", winningTeam.color))

        val enemyHPTaken = losingTeam.players.sumOf { it.settings.maxHealth }
        lobby.messagePlayers(Component.text("${enemyHPTaken}p for enemy hp lost", winningTeam.color))

        return hpLeft + enemyHPTaken
    }

    open fun postGame(winningTeam: BotBowsTeam?) {
        canMove = true
        matchResult.rounds = round
        if (winningTeam == null) {
            lobby.messagePlayers(
                Component.text(
                    "================\n" +
                            "The game ended in a tie after $round round${if (round == 1) "" else "s"}\n" +
                            "================", NamedTextColor.LIGHT_PURPLE
                )
            ) // TODO: make the tie color be defined in the BotBowsMap as "neutralColor". For example, in blaudVwakcy, its purple, bc its between red & blue
        } else {
            matchResult.team1Won = winningTeam == team1
            lobby.messagePlayers(
                Component.text(
                    "================\n" +
                            "TEAM ${winningTeam.displayName.uppercase()} won the game after $round round${if (round == 1) "" else "s"}! GG\n" +
                            "================", winningTeam.color
                )
            )
            showPostGameTitle(winningTeam)
            showPostGameStats(winningTeam)
        }

        if (!TestCommand.debugging) Main.getPlugin().statsService.saveMatchResult(matchResult)

        Main.WORLD.apply {
            isThundering = false
            setStorm(false)
            clearWeatherDuration = 10000
        }
        team1.reset()
        team2.reset()
        lobby.reset()
    }

    private fun showPostGameTitle(winningTeam: BotBowsTeam) {
        val losingTeam = winningTeam.oppositeTeam
        winningTeam.players.forEach {
            it.avatar.showTitle(
                Title.title(
                    Component.text("Victory", winningTeam.color), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
                )
            )
        }
        losingTeam.players.forEach {
            it.avatar.showTitle(
                Title.title(
                    Component.text("Defeat", losingTeam.color), Component.text(""),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
                )
            )
        }
    }

    private fun showPostGameStats(winningTeam: BotBowsTeam?) {
        val statsLocation = winningTeam?.tribunePos?.clone() ?: BotBows.GLOBAL_LOBBY_LOCATION.clone()
        players.forEach { it.teleport(statsLocation) }

        val statsLocY = statsLocation.blockY
        statsLocation.add(statsLocation.getDirection().multiply(10))
        statsLocation.y = (statsLocY + 3).toDouble()

        val resultDisplay = ResultDisplay(statsLocation, matchResult)
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { resultDisplay.remove() }, 60 * 20L)
    }

    fun endGame(ender: BotBowsPlayer) {
        lobby.messagePlayers(Component.text("The game was ended by ").append(ender.name))
        endGame()
    }

    fun endGame() { // the game has ended, check who won
        roundTimer?.cancel()
        startRoundTask?.cancel()
        hazards.forEach { if (it.isActive) it.end() }

        if (team1.points == team2.points) {
            postGame(null)
        } else if (team1.points > team2.points) {
            postGame(team1)
        } else {
            postGame(team2)
        }
    }
}