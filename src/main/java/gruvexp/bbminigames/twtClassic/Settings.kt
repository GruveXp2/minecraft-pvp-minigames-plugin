package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.menu.PlayerListMenu
import gruvexp.bbminigames.menu.menus.*
import gruvexp.bbminigames.model.preset.AbilityPreset
import gruvexp.bbminigames.model.preset.BattlePreset
import gruvexp.bbminigames.model.preset.HealthPreset
import gruvexp.bbminigames.model.preset.WinConditionPreset
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.avatar.NpcAvatar
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import gruvexp.bbminigames.twtClassic.settings.*
import gruvexp.bbminigames.twtClassic.settings.player.PlayerSettings
import gruvexp.bbminigames.twtClassic.team.BotBowsTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import java.util.UUID

class Settings(@JvmField val lobby: Lobby) {
    @JvmField
    var usingExperimentalFeatures: Boolean = false

    @JvmField
    var team1: BotBowsTeam = BotBowsTeam.BLAUD
    @JvmField
    var team2: BotBowsTeam = BotBowsTeam.SAUCE
    private val players: MutableSet<BotBowsPlayer> = mutableSetOf() // liste med alle players som er i gamet

    val healthSettings: HealthSettings = HealthSettings { this.playerSettings }
    val winConditionSettings: WinConditionSettings = WinConditionSettings()
    val hazardSettings: HazardSettings = HazardSettings()
    val abilitySettings: AbilitySettings = AbilitySettings { this.playerSettings }
    val mapSettings: MapSettings = MapSettings(
        { map: BotBowsMap -> onMapChange(map) }, // TODO: gjør om random map tilat man er på tribunepos er i en faktisk lobby og ikke tribune, der man har masse parkor osv
        { triggeredByNewVote: Boolean -> updateLeadingMap(triggeredByNewVote) }
    )

    @JvmField
    var rain: Int = 0 // temporary workaround

    // menus
    lateinit var overviewMenu: OverviewMenu
    lateinit var presetsMenu: PresetsMenu
    @JvmField
    val mapMenus: MutableMap<BotBowsPlayer, MapMenu> = hashMapOf()
    lateinit var healthMenu: HealthMenu
    lateinit var teamsMenu: TeamsMenu
    lateinit var winConditionMenu: WinConditionMenu
    lateinit var hazardMenu: HazardMenu
    @JvmField
    val abilityMenus: MutableMap<BotBowsPlayer, AbilityMenu> = hashMapOf()

    private val playerListMenus: Sequence<PlayerListMenu>
        get() = sequenceOf(teamsMenu, healthMenu) + abilityMenus.values

    private var modPlayer: BotBowsPlayer? = null

    fun initMenus() {
        overviewMenu = OverviewMenu(this)

        presetsMenu = PresetsMenu(this)

        players.forEach { bp: BotBowsPlayer ->
            mapMenus[bp] = MapMenu(this, bp)
            mapSettings.addListener(bp, mapMenus[bp]!!)
        }

        healthMenu = HealthMenu(this)
        healthSettings.listener = healthMenu

        teamsMenu = TeamsMenu(this)

        winConditionMenu = WinConditionMenu(this)
        winConditionSettings.listener = winConditionMenu

        hazardMenu = HazardMenu(this)
        hazardSettings.listener = hazardMenu

        players.forEach { bp ->
            abilityMenus[bp] = AbilityMenu(this, bp)
            abilitySettings.addListener(bp, abilityMenus[bp]!!)
        }

        mapSettings.currentMap = BotBowsMap.RANDOM
    }

    val playerSettings: Iterable<PlayerSettings>
        get() = players.map {it.settings}

    fun saveBattlePreset(presetName: String, presetIcon: Material): BattlePreset {
        val healthPreset = HealthPreset(
            if (healthSettings.isIndividualMaxHealth) null else healthSettings.maxHealth,
            if (healthSettings.isIndividualMaxHealth) {
                players.associate { bp -> bp.avatar.uuid to bp.settings.maxHealth }
            } else null,
            if (healthSettings.isCustomDamage) {
                players.associate { bp -> bp.avatar.uuid to bp.settings.attackDamage }
            } else null
        )
        val winConditionPreset = WinConditionPreset(
            winConditionSettings.winScoreThreshold,
            winConditionSettings.roundDuration,
            winConditionSettings.isDynamicScoring
        )

        val bannedAbilities: Set<AbilityType> = abilitySettings.getBanned()
        val abilityPreset = AbilityPreset(
            if (abilitySettings.isIndividualMax) null else abilitySettings.maxAbilities,
            if (abilitySettings.isIndividualMax) {
                players.associate { bp -> bp.avatar.uuid to bp.settings.maxAbilities}
            } else null,
            if (abilitySettings.isIndividualCooldown) null else abilitySettings.cooldownMultiplier,
            if (abilitySettings.isIndividualCooldown) {
                players.associate { bp -> bp.avatar.uuid to bp.settings.abilityCooldownMultiplier }
            } else null,
            bannedAbilities.ifEmpty { null }
        )
        return BattlePreset(
            presetName,
            presetIcon,
            mapSettings.currentMap,
            team1.players.map { bp -> bp.avatar.uuid }.toSet(),
            team2.players.map { bp -> bp.avatar.uuid }.toSet(),
            healthPreset,
            winConditionPreset,
            hazardSettings.getChances(),
            abilityPreset
        )
    }

    fun applyBattlePreset(preset: BattlePreset) {
        mapSettings.currentMap = preset.map

        preset.team1
            .map { id -> BotBows.getBotBowsPlayer(id) }
            .filter { players.contains(it) }
            .forEach { team1.join(it) }
        preset.team2
            .map { id -> BotBows.getBotBowsPlayer(id) }
            .filter { players.contains(it) }
            .forEach { team2.join(it) }

        preset.health.maxHp?.let { healthSettings.maxHealth = it }

        val individualMaxHp: Map<UUID, Int>? = preset.health.individualMaxHp
        val isIndividualMaxHp = individualMaxHp != null
        healthSettings.isIndividualMaxHealth = isIndividualMaxHp
        if (isIndividualMaxHp) {
            individualMaxHp.forEach { (id: UUID, maxHealth: Int) ->
                BotBows.getBotBowsPlayer(id)?.let { it.settings.maxHealth = maxHealth }
            }
        }
        val individualDamage: Map<UUID, Int>? = preset.health.customDamage
        val isIndividualDamage = individualDamage != null
        healthSettings.isCustomDamage = isIndividualDamage
        if (isIndividualDamage) {
            individualDamage.forEach { (id: UUID, attackDamage: Int) ->
                BotBows.getBotBowsPlayer(id)?.let { it.settings.attackDamage = attackDamage }
            }
        }

        val winConditionPreset = preset.winCondition
        winConditionSettings.winScoreThreshold = winConditionPreset.winScoreThreshold
        winConditionSettings.roundDuration = winConditionPreset.roundDuration
        winConditionSettings.isDynamicScoring = winConditionPreset.dynamicPoints

        val allowedHazards: Set<HazardType> = mapSettings.currentMap.allowedHazards
        allowedHazards.forEach {type: HazardType ->
            hazardSettings.setChance(type, preset.hazards[type]!!)
        }

        val abilityPreset = preset.abilities
        abilitySettings.applyPreset(abilityPreset)

        lobby.messagePlayers(
            Component.text("Applied preset ")
                .append(Component.text(preset.name, NamedTextColor.AQUA))
        )
    }

    private fun onMapChange(map: BotBowsMap) {
        setNewTeams(false)
        hazardSettings.syncWithMap(map)
    }

    fun switchTeam(bp: BotBowsPlayer) {
        bp.team.oppositeTeam.join(bp)
        playerListMenus.forEach { it.updatePlayer(bp) }
    }

    private fun setNewTeams(flipped: Boolean) {
        val team1Players = team1.players.toMutableList()
        val team2Players = team2.players.toMutableList()
        team1.clearPlayers()
        team2.clearPlayers()

        val map = mapSettings.currentMap
        team1 = if (flipped) map.team2 else map.team1
        team2 = if (flipped) map.team1 else map.team2
        team1.putPlayers(team1Players)
        team2.putPlayers(team2Players)

        teamsMenu.registerTeams()
        players.forEach { bp -> playerListMenus.forEach { it.updatePlayer(bp) } }
    }

    private fun updateLeadingMap(triggeredByNewVote: Boolean) {
        val leading = mapSettings.mapVotingSession.getLeadingMaps()
        val mapCount = leading.maps.size
        if (mapCount == 0) return

        if (!leading.maps.contains(mapSettings.currentMap)) {
            val mapsString = leading.maps.joinToString(", ") { it.prettyName() }
            lobby.messagePlayers(
                Component.text((if (triggeredByNewVote) "New" else "Current") + " leading map" + (if (mapCount == 1) "" else "s") + " with ")
                    .append(Component.text(leading.voteCount.toString() + " votes", NamedTextColor.GREEN))
                    .append(Component.text(": ")).append(Component.text(mapsString, NamedTextColor.GOLD))
            )
            if (mapCount == 1) mapSettings.currentMap = leading.maps.first()
        }
    }

    fun finishMapSelection() {
        if (mapSettings.isVoteMode) finishVoting()
    }

    fun finishVoting() {
        if (lobby.isGameActive) return

        val votingSession = mapSettings.mapVotingSession

        if (votingSession.getTotalVotes() == 0) {
            lobby.messagePlayers(Component.text("Nobody voted for a map! A random map will be picked"))
            pickRandomMap()
            return
        }

        if (mapSettings.isWeightedVoting) {
            val winningMap = votingSession.getWinningMapWeighted()

            val winningMapPercent = (votingSession.getVotes(winningMap) * 100) / votingSession.getTotalVotes()
            lobby.messagePlayers(
                Component.empty()
                    .append(Component.text(winningMap.prettyName(), NamedTextColor.AQUA))
                    .append(Component.text(" was picked randomly by weighted vote! (had $winningMapPercent% of the votes)"))
            )

            if (winningMap == BotBowsMap.RANDOM) {
                pickRandomMap()
            } else {
                mapSettings.currentMap = winningMap
            }
        } else {
            val leading = votingSession.getLeadingMaps()
            val mapCount = leading.maps.size
            if (mapCount == 1) {
                val winningMap = leading.maps.first()
                lobby.messagePlayers(
                    Component.empty()
                        .append(Component.text(winningMap.prettyName(), NamedTextColor.AQUA))
                        .append(Component.text(" won the vote with "))
                        .append(Component.text(leading.voteCount, NamedTextColor.GREEN))
                        .append(Component.text(" votes"))
                )
                if (winningMap == BotBowsMap.RANDOM) {
                    pickRandomMap()
                }
            } else {
                val mapsString = leading.maps.joinToString(", ") { it.prettyName() }
                lobby.messagePlayers(
                    Component.text("Vote is tied between ")
                        .append(Component.text(mapsString, NamedTextColor.YELLOW))
                        .append(Component.text(", both have "))
                        .append(Component.text(leading.voteCount, NamedTextColor.GREEN))
                        .append(Component.text(" votes! One of them will be picked randomly"))
                )
                pickRandomMap(leading.maps)
            }
        }
    }

    private fun pickRandomMap(maps: Set<BotBowsMap> = mapSettings.mapVotingSession.classicMapList) {
        val pickedMap = mapSettings.mapVotingSession.pickRandom(maps)
        lobby.messagePlayers(
            Component.empty()
                .append(Component.text(pickedMap.prettyName(), NamedTextColor.AQUA))
                .append(Component.text(" was picked!"))
        )
        mapSettings.currentMap = pickedMap
    }

    fun switchTeamSides() {
        setNewTeams(true)
    }

    fun joinGame(p: Player) {
        val bp = BotBowsPlayer(p, this)
        joinGame(bp)

        if (getPlayers().size == 1 || modPlayer == null || modPlayer!!.avatar is NpcAvatar) {
            modPlayer = bp
            Bukkit.getOnlinePlayers()
                .forEach { it.sendMessage(Component.text("${p.name} has joined BotBows Lobby #${lobby.ID + 1} (${players.size}) and will be the settings moderator")) }
            overviewMenu.open(p)
        } else {
            Bukkit.getOnlinePlayers()
                .forEach { it.sendMessage(Component.text("${p.name} has joined BotBows Lobby #${lobby.ID + 1} (${players.size})")) }
        }
    }

    fun joinGame(mannequin: Mannequin?) {
        val bp = BotBowsPlayer(mannequin, this)
        joinGame(bp)
        Bukkit.getOnlinePlayers()
            .forEach { it.sendMessage(Component.text("${bp.plainName} has joined BotBows Lobby #${lobby.ID + 1} (${players.size})")) }
    }

    private fun joinGame(bp: BotBowsPlayer) {
        lobby.registerBotBowsPlayer(bp)
        players.add(bp)
        if (team1.size() <= team2.size()) { // players fordeles jevnt i lagene
            team1.join(bp)
        } else {
            team2.join(bp)
        }
        playerListMenus.forEach { it.addPlayer(bp) }

        val mapMenu = MapMenu(this, bp)
        mapMenus[bp] = mapMenu
        mapSettings.addListener(bp, mapMenu)

        val abilityMenu = AbilityMenu(this, bp)
        abilityMenus[bp] = abilityMenu
        abilitySettings.addListener(bp, abilityMenu)
        players.forEach { abilityMenu.addPlayer(it) }
        players.forEach { it.settings.addListener(bp, healthMenu, abilityMenu) }
        bp.settings.maxHealth = healthSettings.maxHealth
    }

    fun leaveGame(bp: BotBowsPlayer) {
        if (!players.contains(bp)) {
            bp.avatar.message(Component.text("You cant leave when youre not in a game", NamedTextColor.RED))
            return
        }
        bp.leaveGame()
        players.remove(bp)
        mapSettings.removeListener(bp)
        mapSettings.mapVotingSession.removeVote(bp)

        playerListMenus.forEach { it.removePlayer(bp) }
        abilityMenus.remove(bp)
        mapMenus.remove(bp)
        abilitySettings.removeListener(bp)
        players.forEach { it.settings.removeListener(bp) }
        if (isPlayerMod(bp) && players.isNotEmpty()) {
            for (nextBp in players) {
                if (nextBp.avatar is PlayerAvatar) {
                    setModPlayer(nextBp)
                    break
                }
            }
        }

        bp.avatar.message(Component.text("You left BotBows Lobby #${lobby.ID + 1}", NamedTextColor.YELLOW))
        lobby.messagePlayers(Component.text("${bp.plainName} has left the lobby (${players.size})", NamedTextColor.YELLOW))
        bp.destroy()
    }

    fun getPlayers(): Set<BotBowsPlayer> {
        return players.toSet()
    }

    fun isPlayerJoined(playerId: UUID): Boolean {
        return players.contains(lobby.getBotBowsPlayer(playerId))
    }

    fun setModPlayer(bp: BotBowsPlayer) {
        val first = if (modPlayer == null) "" else " new"
        if (modPlayer != null) abilityMenus[modPlayer]!!.isToggleAbilityMode = false

        modPlayer = bp
        lobby.messagePlayers(
            bp.name.color(NamedTextColor.GREEN).append(Component.text(" is the$first game mod", NamedTextColor.WHITE))
        )
    }

    fun checkMod(bp: BotBowsPlayer): Boolean {
        val isPlayerMod = isPlayerMod(bp)
        if (!isPlayerMod) bp.avatar.message(Component.text("Only mods can do this action", NamedTextColor.RED))
        return isPlayerMod
    }

    fun isPlayerMod(bp: BotBowsPlayer): Boolean {
        return bp === modPlayer
    }
}
