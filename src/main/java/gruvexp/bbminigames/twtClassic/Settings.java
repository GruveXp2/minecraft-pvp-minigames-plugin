package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.model.preset.AbilityPreset;
import gruvexp.bbminigames.model.preset.BattlePreset;
import gruvexp.bbminigames.model.preset.HealthPreset;
import gruvexp.bbminigames.model.preset.WinConditionPreset;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.avatar.NpcAvatar;
import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.map.MapVotingSession;
import gruvexp.bbminigames.twtClassic.map.VoteResult;
import gruvexp.bbminigames.twtClassic.settings.AbilitySettings;
import gruvexp.bbminigames.twtClassic.settings.HazardSettings;
import gruvexp.bbminigames.twtClassic.settings.MapSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Settings {

    public final Lobby lobby;
    public boolean useExperimentalFeatures = false;

    public BotBowsMap currentMap; // default map
    private final MapVotingSession mapVotingSession = new MapVotingSession();

    public BotBowsTeam team1 = new TeamBlaud(); // dersom man endrer team, vil team1 og team2 feks byttes ut med TeamGraut og TeamWacky objekter, ettersom det er forskjell på dem
    public BotBowsTeam team2 = new TeamSauce();
    private final Set<BotBowsPlayer> players = new HashSet<>(); // liste med alle players som er i gamet
    private int maxHP = 3; // hvor mye hp man har hvis custom hp er disabla
    // win condition
    private boolean dynamicScoring = true; // If true, når alle på et lag dauer så gis et poeng for hvert liv som er igjen + totalt liv som er tatt ut
    private int winScoreThreshold = 30; // hvor mange poeng man skal spille til. Hvis den er 0, så fortsetter det for alltid til man tar /stopgame (/botbows stop)
    private int roundDuration = 5;
    // hazards
    private HazardSettings hazardSettings;
    // abilities
    private AbilitySettings abilitySettings;
    public int rain = 0; // temporary workaround
    private MapSettings mapSettings ;
    // menus
    public Map<BotBowsPlayer, MapMenu> mapMenus;
    public MapMenu mapMenu;
    public HealthMenu healthMenu;
    public TeamsMenu teamsMenu;
    public WinConditionMenu winConditionMenu;
    public HazardMenu hazardMenu;
    public Map<BotBowsPlayer, AbilityMenu> abilityMenus;

    private BotBowsPlayer modPlayer;

    public Settings(Lobby lobby) {
        this.lobby = lobby;
    }

    public void initMenus() {
        mapSettings = new MapSettings();
        players.forEach(bp -> {
            mapMenus.put(bp, new MapMenu(this, bp));
            mapSettings.addListener(bp, mapMenus.get(bp));
        });

        healthMenu = new HealthMenu(this);
        healthMenu.disableCustomHP();
        healthMenu.disableCustomDamage();
        setMaxHP(3);

        teamsMenu = new TeamsMenu(this);
        teamsMenu.registerTeams();

        winConditionMenu = new WinConditionMenu(this);
        winConditionMenu.enableDynamicPoints();
        winConditionMenu.updateWinScoreThreshold();
        winConditionMenu.updateRoundDuration();

        hazardMenu = new HazardMenu(this);
        hazardSettings = new HazardSettings(hazardMenu);

        abilityMenus = new HashMap<>();
        abilitySettings = new AbilitySettings();
        players.forEach(bp -> {
            abilityMenus.put(bp, new AbilityMenu(this, bp));
            abilitySettings.addListener(bp, abilityMenus.get(bp));
        });

        setMap(BotBowsMap.CLASSIC_ARENA);
    }

    public HazardSettings getHazardSettings() {
        return hazardSettings;
    }

    public AbilitySettings getAbilitySettings() {
        return abilitySettings;
    }

    public MapSettings getMapSettings() {
        return mapSettings;
    }

    public MapVotingSession getMapVotingSession() {return mapVotingSession;}

    public BattlePreset saveBattlePreset(String presetName, Material presetIcon) {
        HealthPreset healthPreset = new HealthPreset(
                healthMenu.isCustomHPEnabled() ? null : maxHP,
                healthMenu.isCustomHPEnabled() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), BotBowsPlayer::getMaxHP)) : null,
                healthMenu.isCustomDamageEnabled() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), BotBowsPlayer::getAttackDamage)) : null
        );
        WinConditionPreset winConditionPreset = new WinConditionPreset(
                winScoreThreshold, roundDuration, dynamicScoring
        );

        Set<AbilityType> bannedAbilities = abilitySettings.getBanned();
        AbilityPreset abilityPreset = new AbilityPreset(
                abilitySettings.isIndividualMax() ? null : abilitySettings.getMaxAbilities(),
                abilitySettings.isIndividualMax() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), BotBowsPlayer::getMaxAbilities)) : null,
                abilitySettings.isIndividualCooldown() ? null : abilitySettings.getCooldownMultiplier(),
                abilitySettings.isIndividualCooldown() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), BotBowsPlayer::getAbilityCooldownMultiplier)) : null,
                !bannedAbilities.isEmpty() ? bannedAbilities : null
        );
        return new BattlePreset(
                presetName,
                presetIcon,
                currentMap,
                team1.getPlayers().stream().map(p -> p.avatar.getUUID()).collect(Collectors.toSet()),
                team2.getPlayers().stream().map(p -> p.avatar.getUUID()).collect(Collectors.toSet()),
                healthPreset,
                winConditionPreset,
                hazardSettings.getChances(),
                abilityPreset
        );
    }

    public void applyBattlePreset(BattlePreset preset) {
        setMap(preset.map());

        preset.team1().stream()
                .map(BotBows::getBotBowsPlayer)
                .filter(Objects::nonNull)
                .filter(players::contains)
                .forEach(team1::join);
        preset.team2().stream()
                .map(BotBows::getBotBowsPlayer)
                .filter(Objects::nonNull)
                .filter(players::contains)
                .forEach(team2::join);

        HealthPreset healthPreset = preset.health();
        Integer maxHp = healthPreset.maxHp();
        if (maxHp != null) {
            setMaxHP(maxHp);
        }
        var individualMaxHp = healthPreset.individualMaxHp();
        if (individualMaxHp != null) {
            individualMaxHp.forEach((key, value) -> {
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(key);
                if (bp != null) {
                    bp.setMaxHP(value);
                }
            });
        }
        var individualDamage = healthPreset.customDamage();
        if (individualDamage != null) {
            individualDamage.forEach((key, value) -> {
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(key);
                if (bp != null) {
                    bp.setAttackDamage(value);
                }
            });
        }

        WinConditionPreset winConditionPreset = preset.winCondition();
        setWinScoreThreshold(winConditionPreset.winScoreThreshold());
        setRoundDuration(winConditionPreset.roundDuration());
        setDynamicScoring(winConditionPreset.dynamicPoints());

        currentMap.allowedHazards.forEach(type -> hazardSettings.setChance(type, preset.hazards().get(type)));

        AbilityPreset abilityPreset = preset.abilities();
        abilitySettings.applyPreset(abilityPreset);
    }

    public void setMap(BotBowsMap map) {
        if (map == currentMap) return;
        currentMap = map;
        switch (map) {
            case CLASSIC_ARENA -> setNewTeams(new TeamBlaud(team1), new TeamSauce(team2));
            case ICY_RAVINE -> setNewTeams(new TeamGraut(team1), new TeamWacky(team2));
            case ROYAL_CASTLE -> setNewTeams(new TeamKjødd(team1), new TeamGoofy(team2));
            case PIGLIN_HIDEOUT -> setNewTeams(new TeamPiglin(team1), new TeamHoglin(team2));
            case STEAMPUNK -> setNewTeams(new TeamBlocc(team1), new TeamQuicc(team2));
            case INSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Corner", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -58.5, 30, -212.5, 180, -10), new Location(Main.WORLD, -29.5, 27, -211, 180, 10), team1),
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -6.5, 6, -264.5, 45, 30), new Location(Main.WORLD, -29.5, 27, -273, 0, -10), team2));
            case OUTSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -75.5, 26, -259.5, 45, -20), new Location(Main.WORLD, -67.5, 24.5, -267.5, -315, 15), team1),
                    new BotBowsTeam("Mountain", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -106, 15.50, -205, 180, 0), new Location(Main.WORLD, -109.5, 28, -220.5, -150, 15), team2));
            case ROCKET_FOREST -> setNewTeams(
                    new BotBowsTeam("Door", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -75, 4, -201.5, 0, 10), new Location(Main.WORLD, -70.5, 15, -197, -25, 33), team1),
                    new BotBowsTeam("Tunnel", NamedTextColor.DARK_GREEN, DyeColor.GREEN, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -34, 11.5, -197, 33, 0), new Location(Main.WORLD, -18.5, 29, -193.5, 55, 15), team2));
            case ROCKET -> setNewTeams(
                    new BotBowsTeam("Dropper", NamedTextColor.BLACK, DyeColor.BLACK, TeamSide.TEAM_1,
                            new Location(Main.WORLD, 4.5, 74, 18.5, 0, 60), new Location(Main.WORLD, 1.5, 58, 20.5, -40, 17), team1),
                    new BotBowsTeam("Engine", NamedTextColor.RED, DyeColor.RED, TeamSide.TEAM_2,
                            new Location(Main.WORLD, 2.5, 47, 36.5, 135, 10), new Location(Main.WORLD, -5.5, 50, 39.5, -130, 15), team2));
            case SPACE_STATION -> setNewTeams(new TeamCold(team1), new TeamWarm(team2));
        }
        team1.postTeamSwap();
        team2.postTeamSwap();
        teamsMenu.registerTeams();
        teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        healthMenu.updateMenu(); // update so the name colors match the new team color

        hazardSettings.syncWithMap(map);

        String mapName = map.name().charAt(0) + map.name().substring(1).toLowerCase().replace('_', ' ');
        lobby.messagePlayers(Component.text("Map set to ").append(Component.text(mapName, NamedTextColor.GREEN)));
    }

    public void finishVoting() {
        if (lobby.isGameActive()) return;

        VoteResult result = mapVotingSession.getLeadingMap();
        lobby.messagePlayers(Component.empty()
                .append(Component.text(result.getMap().name().toLowerCase(), NamedTextColor.AQUA))
                .append(Component.text(" won the vote with "))
                .append(Component.text(result.getVoteCount(), NamedTextColor.GREEN))
                .append(Component.text(" votes")));
        setMap(result.getMap());
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
        team1.setOppositeTeam(team2);
        team2.setOppositeTeam(team1);
    }

    public void joinGame(Player p) {
        BotBowsPlayer bp = new BotBowsPlayer(p, this);
        lobby.registerBotBowsPlayer(bp);
        players.add(bp);
        if (team1.size() <= team2.size()) { // players fordeles jevnt i lagene
            team1.join(bp);
        } else {
            team2.join(bp);
        }
        teamsMenu.recalculateTeam();
        bp.setMaxHP(maxHP);
        healthMenu.updateMenu();
        abilityMenus.values().forEach(menu -> menu.addPlayer(bp));
        AbilityMenu newMenu = new AbilityMenu(this, bp);
        abilityMenus.put(bp, newMenu);
        abilitySettings.addListener(bp, newMenu);
        players.forEach(newMenu::addPlayer);

        if (getPlayers().size() == 1 || modPlayer == null || modPlayer.avatar instanceof NpcAvatar) {
            modPlayer = bp;
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(p.getName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")" +
                    " and will be the settings moderator")));
            mapMenu.open(p);
        } else {
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(p.getName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")")));
        }
    }

    public void joinGame(Mannequin mannequin) {
        BotBowsPlayer bp = new BotBowsPlayer(mannequin, this);
        lobby.registerBotBowsPlayer(bp);
        players.add(bp);
        if (team1.size() <= team2.size()) { // players fordeles jevnt i lagene
            team1.join(bp);
        } else {
            team2.join(bp);
        }
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();
        abilityMenus.values().forEach(menu -> menu.addPlayer(bp));
        AbilityMenu newMenu = new AbilityMenu(this, bp);
        abilityMenus.put(bp, newMenu);
        abilitySettings.addListener(bp, newMenu);
        players.forEach(newMenu::addPlayer);
        Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(bp.getPlainName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")")));
    }

    public void leaveGame(BotBowsPlayer bp) {
        if (!players.contains(bp)) {
            bp.avatar.message(Component.text("You can't leave when you're not in a game", NamedTextColor.RED));
            return;
        }
        bp.leaveGame();
        players.remove(bp);
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();
        abilityMenus.values().forEach(menu -> menu.removePlayer(bp));
        abilityMenus.remove(bp);
        abilitySettings.removeListener(bp);
        if (playerIsMod(bp) && !players.isEmpty()) {
            setModPlayer(players.iterator().next());
        }

        bp.reset();
        bp.avatar.message(Component.text("You left BotBows Lobby #" + (lobby.ID + 1), NamedTextColor.YELLOW));
        lobby.messagePlayers(Component.text(bp.getPlainName() + " has left the lobby (" + players.size() + ")", NamedTextColor.YELLOW));
    }

    public Set<BotBowsPlayer> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isPlayerJoined(UUID playerId) {
        return Optional.ofNullable(lobby.getBotBowsPlayer(playerId))
                .map(players::contains)
                .orElse(false);
    }

    public void setModPlayer(BotBowsPlayer bp) {
        String first = modPlayer == null ? "" : "new ";
        modPlayer = bp;
        lobby.messagePlayers(bp.getName().color(NamedTextColor.GREEN).append(Component.text(" is the " + first + "game mod", NamedTextColor.WHITE)));
    }

    public boolean playerIsMod(BotBowsPlayer bp) {
        boolean isPlayerMod = bp == modPlayer;
        if (!isPlayerMod) bp.avatar.message(Component.text("Only mods can do this action", NamedTextColor.RED));
        return isPlayerMod;
    }

    public void setDynamicScoring(boolean dynamicScoring) {
        this.dynamicScoring = dynamicScoring;
    }

    public boolean dynamicScoringEnabled() {
        return dynamicScoring;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        players.forEach(p -> p.setMaxHP(maxHP));
        healthMenu.updateMenu();
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void resetAttackDamage() {
        players.forEach(p -> p.setAttackDamage(1));
    }

    public int getWinScoreThreshold() {
        return winScoreThreshold;
    }

    public void changeWinScoreThreshold(int Δthreshold) {
        setWinScoreThreshold(winScoreThreshold + Δthreshold);
    }

    public void setWinScoreThreshold(int threshold) {
        winScoreThreshold = Math.max(threshold, 0);
        winConditionMenu.updateWinScoreThreshold();
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void changeRoundDuration(int Δduration) {
        setRoundDuration(roundDuration + Δduration);
    }

    public void setRoundDuration(int duration) {
        roundDuration = Math.max(duration, 0);
        winConditionMenu.updateRoundDuration();
    }
}
