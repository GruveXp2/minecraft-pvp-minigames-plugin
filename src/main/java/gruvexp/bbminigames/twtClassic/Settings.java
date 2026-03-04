package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;

import java.util.*;

public class Settings {

    public final Lobby lobby;
    public boolean useExperimentalFeatures = false;

    public BotBowsMap currentMap; // default map

    public BotBowsTeam team1 = new TeamBlaud(); // dersom man endrer team, vil team1 og team2 feks byttes ut med TeamGraut og TeamWacky objekter, ettersom det er forskjell på dem
    public BotBowsTeam team2 = new TeamSauce();
    private final Set<BotBowsPlayer> players = new HashSet<>(); // liste med alle players som er i gamet
    private int maxHP = 3; // hvor mye hp man har hvis custom hp er disabla
    // win condition
    private boolean dynamicScoring = true; // If true, når alle på et lag dauer så gis et poeng for hvert liv som er igjen + totalt liv som er tatt ut
    private int winScoreThreshold = 30; // hvor mange poeng man skal spille til. Hvis den er 0, så fortsetter det for alltid til man tar /stopgame (/botbows stop)
    private int roundDuration = 5;
    // hazards
    private final HashMap<HazardType, Hazard> hazards = new HashMap<>();
    // abilities
    private int maxAbilities = 0;
    private boolean isIndividualMaxAbilities = false;
    private float abilityCooldownMultiplier = 1.0f;
    private boolean isIndividualCooldownMultiplier = false;
    private final Map<AbilityType, Boolean> abilityStates = new HashMap<>();
    public int rain = 0;
    // menus
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
        mapMenu = new MapMenu(this);

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

        abilityMenus = new HashMap<>();
        players.forEach(bp -> abilityMenus.put(bp, new AbilityMenu(this, bp)));

        setMap(BotBowsMap.CLASSIC_ARENA);
    }

    public void setMap(BotBowsMap map) {
        if (map == currentMap) return;
        currentMap = map;
        switch (map) {
            case CLASSIC_ARENA -> setNewTeams(new TeamBlaud(team1), new TeamSauce(team2));
            case ICY_RAVINE -> setNewTeams(new TeamGraut(team1), new TeamWacky(team2));
            case ROYAL_MAP -> setNewTeams(new TeamKjødd(team1), new TeamGoofy(team2));
            case PIGLIN_HIDEOUT -> setNewTeams(new TeamPiglin(team1), new TeamHoglin(team2));
            case STEAMPUNK -> setNewTeams(new TeamBlocc(team1), new TeamQuicc(team2));
            case INSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Corner", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY,
                            new Location(Main.WORLD, -58.5, 30, -212.5, 180, -10), new Location(Main.WORLD, -29.5, 27, -211, 180, 10), team1),
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME,
                            new Location(Main.WORLD, -6.5, 6, -264.5, 45, 30), new Location(Main.WORLD, -29.5, 27, -273, 0, -10), team2));
            case OUTSIDE_BOTBASE -> setNewTeams(
                    new BotBowsTeam("Core", NamedTextColor.GREEN, DyeColor.LIME,
                            new Location(Main.WORLD, -75.5, 26, -259.5, 45, -20), new Location(Main.WORLD, -67.5, 24.5, -267.5, -315, 15), team1),
                    new BotBowsTeam("Mountain", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE,
                            new Location(Main.WORLD, -106, 15.50, -205, 180, 0), new Location(Main.WORLD, -109.5, 28, -220.5, -150, 15), team2));
            case ROCKET_FOREST -> setNewTeams(
                    new BotBowsTeam("Door", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY,
                            new Location(Main.WORLD, -75, 4, -201.5, 0, 10), new Location(Main.WORLD, -70.5, 15, -197, -25, 33), team1),
                    new BotBowsTeam("Tunnel", NamedTextColor.DARK_GREEN, DyeColor.GREEN,
                            new Location(Main.WORLD, -34, 11.5, -197, 33, 0), new Location(Main.WORLD, -18.5, 29, -193.5, 55, 15), team2));
            case ROCKET -> setNewTeams(
                    new BotBowsTeam("Dropper", NamedTextColor.BLACK, DyeColor.BLACK,
                            new Location(Main.WORLD, 4.5, 74, 18.5, 0, 60), new Location(Main.WORLD, 1.5, 58, 20.5, -40, 17), team1),
                    new BotBowsTeam("Engine", NamedTextColor.RED, DyeColor.RED,
                            new Location(Main.WORLD, 2.5, 47, 36.5, 135, 10), new Location(Main.WORLD, -5.5, 50, 39.5, -130, 15), team2));
            case SPACE_STATION -> setNewTeams(new TeamCold(team1), new TeamWarm(team2));
        }
        team1.postTeamSwap();
        team2.postTeamSwap();
        teamsMenu.registerTeams();
        teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        healthMenu.updateMenu(); // update so the name colors match the new team color

        hazards.keySet().retainAll(map.allowedHazards); // remove hazards not compatible with the new map
        map.allowedHazards.forEach(newH -> hazards.computeIfAbsent(newH, h -> h.createHazard(lobby))); // add hazards compatible with the new map
        hazardMenu.updateHazards();

        String mapName = map.name().charAt(0) + map.name().substring(1).toLowerCase().replace('_', ' ');
        lobby.messagePlayers(Component.text("Map set to ").append(Component.text(mapName, NamedTextColor.GREEN)));
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
        team1.setOppositeTeam(team2);
        team2.setOppositeTeam(team1);
    }

    public HashMap<HazardType, Hazard> getHazards() {
        return hazards;
    }

    public Hazard getHazard(HazardType type) {
        return hazards.get(type);
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
        players.forEach(newMenu::addPlayer);

        if (getPlayers().size() == 1) {
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

    public void setMaxAbilities(int maxAbilities) {
        if (this.maxAbilities == 0 && maxAbilities > 0) { // if max abilities was increased from 0, abilities gets enabled, so update the ui
            this.maxAbilities = maxAbilities;
            abilityMenus.values().forEach(AbilityMenu::updateAbilityUIState);
        }
        this.maxAbilities = maxAbilities;
        players.forEach(p -> p.setMaxAbilities(maxAbilities));
        if (maxAbilities == 0) { // if max abilities is set to 0, abilities gets disabled, so update the ui
            abilityMenus.values().forEach(AbilityMenu::updateAbilityUIState);
        }
        abilityMenus.values().forEach(AbilityMenu::updateMaxAbilities);
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public void enableIndividualMaxAbilities() {
        isIndividualMaxAbilities = true;
        abilityMenus.values().forEach(AbilityMenu::updateMaxAbilitiesUIState);
    }

    public void disableIndividualMaxAbilities() {
        isIndividualMaxAbilities = false;
        abilityMenus.values().forEach(AbilityMenu::updateMaxAbilitiesUIState);
    }

    public boolean individualMaxAbilitiesOn() {
        return isIndividualMaxAbilities;
    }

    public void setAbilityCooldownMultiplier(float cooldownMultiplier) {
        abilityCooldownMultiplier = cooldownMultiplier;
        players.forEach(p -> p.setAbilityCooldownMultiplier(cooldownMultiplier));
        abilityMenus.values().forEach(AbilityMenu::updateCooldownMultiplier);
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }

    public void enableIndividualCooldownMultiplier() {
        isIndividualCooldownMultiplier = true;
        abilityMenus.values().forEach(AbilityMenu::updateCooldownMultiplierUIState);
    }

    public void disableIndividualCooldownMultiplier() {
        isIndividualCooldownMultiplier = false;
        abilityMenus.values().forEach(AbilityMenu::updateCooldownMultiplierUIState);
    }

    public boolean individualCooldownMultiplierOn() {
        return isIndividualCooldownMultiplier;
    }

    public void allowAbility(AbilityType type) {
        abilityStates.put(type, true);
        abilityMenus.values().forEach(menu -> menu.updateAbilityStatus(type));
    }

    public void disableAbility(AbilityType type) {
        abilityStates.put(type, false);
        abilityMenus.values().forEach(menu -> menu.updateAbilityStatus(type));
        players.forEach(p -> p.unequipAbility(type));
    }

    public void toggleAbility(AbilityType type) {
        if (isAbilityAllowed(type)) {
            disableAbility(type);
        } else {
            allowAbility(type);
        }
    }

    public boolean isAbilityAllowed(AbilityType type) {
        return abilityStates.getOrDefault(type, true); // Default to enabled
    }
}
