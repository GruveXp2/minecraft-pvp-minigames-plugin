package gruvexp.bbminigames.twtClassic;

import com.google.common.collect.ImmutableSet;
import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.model.preset.AbilityPreset;
import gruvexp.bbminigames.model.preset.BattlePreset;
import gruvexp.bbminigames.model.preset.HealthPreset;
import gruvexp.bbminigames.model.preset.WinConditionPreset;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.avatar.NpcAvatar;
import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
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

    public BotBowsTeam team1 = new TeamBlaud(); // dersom man endrer team, vil team1 og team2 feks byttes ut med TeamGraut og TeamWacky objekter, ettersom det er forskjell på dem
    public BotBowsTeam team2 = new TeamSauce();
    private final Set<BotBowsPlayer> players = new HashSet<>(); // liste med alle players som er i gamet
    private int maxHP = 3; // hvor mye hp man har hvis custom hp er disabla
    // health
    private boolean customHP;
    private boolean customDamage;
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
    public final Map<BotBowsPlayer, MapMenu> mapMenus = new HashMap<>();
    public HealthMenu healthMenu;
    public TeamsMenu teamsMenu;
    public WinConditionMenu winConditionMenu;
    public HazardMenu hazardMenu;
    public final Map<BotBowsPlayer, AbilityMenu> abilityMenus = new HashMap<>();

    private BotBowsPlayer modPlayer;

    public Settings(Lobby lobby) {
        this.lobby = lobby;
    }

    public void initMenus() {
        mapSettings = new MapSettings(map -> {
            onMapChange(map); // TODO: gjør om random map tilat man er på tribunepos er i en faktisk lobby og ikke tribune, der man har masse parkor osv
            return kotlin.Unit.INSTANCE; // void cant be returned in kotlin
        }, triggeredByNewVote -> {
            updateLeadingMap(triggeredByNewVote);
            return kotlin.Unit.INSTANCE;
        });

        players.forEach(bp -> {
            mapMenus.put(bp, new MapMenu(this, bp));
            mapSettings.addListener(bp, mapMenus.get(bp));
        });

        healthMenu = new HealthMenu(this);
        setCustomHPEnabled(false);
        setCustomDamageEnabled(false);

        teamsMenu = new TeamsMenu(this);
        teamsMenu.registerTeams();

        winConditionMenu = new WinConditionMenu(this);
        winConditionMenu.enableDynamicPoints();
        winConditionMenu.updateWinScoreThreshold();
        winConditionMenu.updateRoundDuration();

        hazardMenu = new HazardMenu(this);
        hazardSettings = new HazardSettings(hazardMenu);

        abilitySettings = new AbilitySettings();
        players.forEach(bp -> {
            abilityMenus.put(bp, new AbilityMenu(this, bp));
            abilitySettings.addListener(bp, abilityMenus.get(bp));
        });

        mapSettings.setCurrentMap(BotBowsMap.RANDOM);
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

    public BattlePreset saveBattlePreset(String presetName, Material presetIcon) {
        HealthPreset healthPreset = new HealthPreset(
                isCustomHPEnabled() ? null : maxHP,
                isCustomHPEnabled() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), bp -> bp.settings.getMaxHp())) : null,
                isCustomDamageEnabled() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), bp -> bp.settings.getAttackDamage())) : null
        );
        WinConditionPreset winConditionPreset = new WinConditionPreset(
                winScoreThreshold, roundDuration, dynamicScoring
        );

        Set<AbilityType> bannedAbilities = abilitySettings.getBanned();
        AbilityPreset abilityPreset = new AbilityPreset(
                abilitySettings.isIndividualMax() ? null : abilitySettings.getMaxAbilities(),
                abilitySettings.isIndividualMax() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), bp -> bp.settings.getMaxAbilities())) : null,
                abilitySettings.isIndividualCooldown() ? null : abilitySettings.getCooldownMultiplier(),
                abilitySettings.isIndividualCooldown() ? players.stream().collect(Collectors.toMap(p -> p.avatar.getUUID(), bp -> bp.settings.getAbilityCooldownMultiplier())) : null,
                !bannedAbilities.isEmpty() ? bannedAbilities : null
        );
        return new BattlePreset(
                presetName,
                presetIcon,
                mapSettings.getCurrentMap(),
                team1.getPlayers().stream().map(p -> p.avatar.getUUID()).collect(Collectors.toSet()),
                team2.getPlayers().stream().map(p -> p.avatar.getUUID()).collect(Collectors.toSet()),
                healthPreset,
                winConditionPreset,
                hazardSettings.getChances(),
                abilityPreset
        );
    }

    public void applyBattlePreset(BattlePreset preset) {
        mapSettings.setCurrentMap(preset.map());

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
        boolean isIndividualMaxHp = individualMaxHp != null;
        setCustomHPEnabled(isIndividualMaxHp);
        if (isIndividualMaxHp) {
            individualMaxHp.forEach((key, value) -> {
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(key);
                if (bp != null) {
                    bp.settings.setMaxHp(value);
                }
            });
        }
        var individualDamage = healthPreset.customDamage();
        boolean isIndividualDamage = individualDamage != null;
        setCustomDamageEnabled(isIndividualDamage);
        if (isIndividualDamage) {
            individualDamage.forEach((key, value) -> {
                BotBowsPlayer bp = BotBows.getBotBowsPlayer(key);
                if (bp != null) {
                    bp.settings.setAttackDamage(value);
                }
            });
        }

        WinConditionPreset winConditionPreset = preset.winCondition();
        setWinScoreThreshold(winConditionPreset.winScoreThreshold());
        setRoundDuration(winConditionPreset.roundDuration());
        setDynamicScoring(winConditionPreset.dynamicPoints());

        ImmutableSet<HazardType> allowedHazards = getMapSettings().getCurrentMap().allowedHazards;
        allowedHazards.forEach(type -> hazardSettings.setChance(type, preset.hazards().get(type)));

        AbilityPreset abilityPreset = preset.abilities();
        abilitySettings.applyPreset(abilityPreset);
    }

    private void onMapChange(BotBowsMap map) {
        setNewTeams(false);
        hazardSettings.syncWithMap(map);
    }

    private void setNewTeams(boolean flipped) {
        BotBowsTeam newTeam1 = flipped ? team2 : team1;
        BotBowsTeam newTeam2 = flipped ? team1 : team2;
        switch (mapSettings.getCurrentMap()) {
            case CLASSIC_ARENA, RANDOM -> setNewTeams(new TeamBlaud(newTeam1), new TeamSauce(newTeam2));
            case ICY_RAVINE -> setNewTeams(new TeamGraut(newTeam1), new TeamWacky(newTeam2));
            case ROYAL_CASTLE -> setNewTeams(new TeamKjødd(newTeam1), new TeamGoofy(newTeam2));
            case PIGLIN_HIDEOUT -> setNewTeams(new TeamPiglin(newTeam1), new TeamHoglin(newTeam2));
            case STEAMPUNK -> setNewTeams(new TeamBlocc(newTeam1), new TeamQuicc(newTeam2));
            case INSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Corner", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -58.5, 30, -212.5, 180, -10), new Location(Main.WORLD, -29.5, 27, -211, 180, 10), newTeam1),
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -6.5, 6, -264.5, 45, 30), new Location(Main.WORLD, -29.5, 27, -273, 0, -10), newTeam2));
            case OUTSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -75.5, 26, -259.5, 45, -20), new Location(Main.WORLD, -67.5, 24.5, -267.5, -315, 15), newTeam1),
                    new BotBowsTeam("Mountain", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -106, 15.50, -205, 180, 0), new Location(Main.WORLD, -109.5, 28, -220.5, -150, 15), newTeam2));
            case ROCKET_FOREST -> setNewTeams(
                    new BotBowsTeam("Door", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, TeamSide.TEAM_1,
                            new Location(Main.WORLD, -75, 4, -201.5, 0, 10), new Location(Main.WORLD, -70.5, 15, -197, -25, 33), newTeam1),
                    new BotBowsTeam("Tunnel", NamedTextColor.DARK_GREEN, DyeColor.GREEN, TeamSide.TEAM_2,
                            new Location(Main.WORLD, -34, 11.5, -197, 33, 0), new Location(Main.WORLD, -18.5, 29, -193.5, 55, 15), newTeam2));
            case ROCKET -> setNewTeams(
                    new BotBowsTeam("Dropper", NamedTextColor.BLACK, DyeColor.BLACK, TeamSide.TEAM_1,
                            new Location(Main.WORLD, 4.5, 74, 18.5, 0, 60), new Location(Main.WORLD, 1.5, 58, 20.5, -40, 17), newTeam1),
                    new BotBowsTeam("Engine", NamedTextColor.RED, DyeColor.RED, TeamSide.TEAM_2,
                            new Location(Main.WORLD, 2.5, 47, 36.5, 135, 10), new Location(Main.WORLD, -5.5, 50, 39.5, -130, 15), newTeam2));
            case SPACE_STATION -> setNewTeams(new TeamCold(newTeam1), new TeamWarm(newTeam2));
        }
        team1.postTeamSwap();
        team2.postTeamSwap();
        teamsMenu.registerTeams();
        teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        healthMenu.updateMenu(); // update so the name colors match the new team color
    }

    private void updateLeadingMap(boolean triggeredByNewVote) {
        VoteResult leading = mapSettings.getMapVotingSession().getLeadingMaps();
        Set<BotBowsMap> leadingMaps = leading.getMaps();
        int mapCount = leadingMaps.size();
        if (mapCount == 0) return;

        if (!leadingMaps.contains(mapSettings.getCurrentMap())) {
            String mapsString = leadingMaps.stream().map(BotBowsMap::prettyName).collect(Collectors.joining(", "));
            lobby.messagePlayers(Component.text((triggeredByNewVote ? "New" : "Current") + " leading map" + (mapCount == 1 ? "" : "s") + " with ")
                    .append(Component.text(leading.getVoteCount() + " votes", NamedTextColor.GREEN))
                    .append(Component.text(": ")).append(Component.text(mapsString, NamedTextColor.GOLD)));
            if (mapCount == 1) mapSettings.setCurrentMap(leadingMaps.iterator().next());
        }
    }

    public void finishMapSelection() {
        if (mapSettings.isVoteMode()) finishVoting();
    }

    public void finishVoting() {
        if (lobby.isGameActive()) return;

        VoteResult leading = mapSettings.getMapVotingSession().getLeadingMaps();
        Set<BotBowsMap> leadingMaps = leading.getMaps();
        int mapCount = leadingMaps.size();
        if (mapCount == 1) {
            BotBowsMap winningMap = leadingMaps.iterator().next();
            lobby.messagePlayers(Component.empty()
                    .append(Component.text(winningMap.prettyName(), NamedTextColor.AQUA))
                    .append(Component.text(" won the vote with "))
                    .append(Component.text(leading.getVoteCount(), NamedTextColor.GREEN))
                    .append(Component.text(" votes")));
            if (winningMap == BotBowsMap.RANDOM) {
                pickRandomMap();
            }
        } else if (mapCount > 1) {
            String mapsString = leadingMaps.stream().map(BotBowsMap::prettyName).collect(Collectors.joining(", "));
            lobby.messagePlayers(Component.text("Vote is tied between ")
                    .append(Component.text(mapsString, NamedTextColor.YELLOW))
                    .append(Component.text(", both have "))
                    .append(Component.text(leading.getVoteCount(), NamedTextColor.GREEN))
                    .append(Component.text(" votes! One of them will be picked randomly")));
            pickRandomMap(leadingMaps);
        } else {
            lobby.messagePlayers(Component.text("Nobody voted for a map! A random map will be picked"));
            pickRandomMap();
        }
    }

    private void pickRandomMap() {
        Set<BotBowsMap> classicMaps = mapSettings.getMapVotingSession().getClassicMapList();
        pickRandomMap(classicMaps);
    }

    private void pickRandomMap(Set<BotBowsMap> maps) {
        BotBowsMap pickedMap = maps.stream().skip(new Random().nextInt(maps.size())).findFirst().orElse(BotBowsMap.RANDOM);
        lobby.messagePlayers(Component.empty()
                .append(Component.text(pickedMap.prettyName(), NamedTextColor.AQUA))
                .append(Component.text(" was picked!")));
        mapSettings.setCurrentMap(pickedMap);
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
        team1.setOppositeTeam(team2);
        team2.setOppositeTeam(team1);
    }

    public void switchTeamSides() {
        setNewTeams(true);
    }

    public void joinGame(Player p) {
        BotBowsPlayer bp = new BotBowsPlayer(p, this);
        joinGame(bp);

        if (getPlayers().size() == 1 || modPlayer == null || modPlayer.avatar instanceof NpcAvatar) {
            modPlayer = bp;
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(p.getName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")" +
                    " and will be the settings moderator")));
            mapMenus.get(bp).open(p);
        } else {
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(p.getName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")")));
        }
    }

    public void joinGame(Mannequin mannequin) {
        BotBowsPlayer bp = new BotBowsPlayer(mannequin, this);
        joinGame(bp);
        Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(Component.text(bp.getPlainName() + " has joined BotBows Lobby #" + (lobby.ID + 1) + " (" + players.size() + ")")));
    }

    private void joinGame(BotBowsPlayer bp) {
        lobby.registerBotBowsPlayer(bp);
        players.add(bp);
        if (team1.size() <= team2.size()) { // players fordeles jevnt i lagene
            team1.join(bp);
        } else {
            team2.join(bp);
        }
        teamsMenu.recalculateTeam();

        MapMenu mapMenu = new MapMenu(this, bp);
        mapMenus.put(bp, mapMenu);
        mapSettings.addListener(bp, mapMenu);

        abilityMenus.values().forEach(menu -> menu.addPlayer(bp));
        AbilityMenu abilityMenu = new AbilityMenu(this, bp);
        abilityMenus.put(bp, abilityMenu);
        abilitySettings.addListener(bp, abilityMenu);
        players.forEach(abilityMenu::addPlayer);
        bp.settings.setMaxHp(maxHP);
    }

    public void leaveGame(BotBowsPlayer bp) {
        if (!players.contains(bp)) {
            bp.avatar.message(Component.text("You can't leave when you're not in a game", NamedTextColor.RED));
            return;
        }
        bp.leaveGame();
        players.remove(bp);
        mapSettings.removeListener(bp);
        mapSettings.getMapVotingSession().removeVote(bp);
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();
        abilityMenus.values().forEach(menu -> menu.removePlayer(bp));
        abilityMenus.remove(bp);
        mapMenus.remove(bp);
        abilitySettings.removeListener(bp);
        if (isPlayerMod(bp) && !players.isEmpty()) {
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
        if (modPlayer != null && modPlayer.isToggleAbilityMode()) modPlayer.disableAbilityToggle();

        modPlayer = bp;
        lobby.messagePlayers(bp.getName().color(NamedTextColor.GREEN).append(Component.text(" is the " + first + "game mod", NamedTextColor.WHITE)));
    }

    public boolean checkMod(BotBowsPlayer bp) {
        boolean isPlayerMod = isPlayerMod(bp);
        if (!isPlayerMod) bp.avatar.message(Component.text("Only mods can do this action", NamedTextColor.RED));
        return isPlayerMod;
    }

    public boolean isPlayerMod(BotBowsPlayer bp) {
        return bp == modPlayer;
    }

    public boolean isCustomHPEnabled() {
        return customHP;
    }

    public void setCustomHPEnabled(boolean enabled) {
        this.customHP = enabled;
        if (!enabled) setMaxHP(3);
        healthMenu.onCustomHPToggle();
    }

    public boolean isCustomDamageEnabled() {
        return customDamage;
    }

    public void setCustomDamageEnabled(boolean enabled) {
        this.customDamage = enabled;
        if (!enabled) resetAttackDamage();
        healthMenu.onCustomDamageToggle();
    }

    public void setDynamicScoring(boolean enabled) {
        this.dynamicScoring = enabled;
    }

    public boolean isDynamicScoringEnabled() {
        return dynamicScoring;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        players.forEach(p -> p.settings.setMaxHp(maxHP));
        healthMenu.updateMenu();
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void resetAttackDamage() {
        players.forEach(p -> p.settings.setAttackDamage(1));
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
