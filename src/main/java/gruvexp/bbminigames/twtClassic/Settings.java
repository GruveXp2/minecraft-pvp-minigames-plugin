package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;

public class Settings {

    public final Lobby lobby;

    public BotBowsMap currentMap = BotBowsMap.CLASSIC_ARENA; // default map

    public BotBowsTeam team1 = new TeamBlaud(); // dersom man endrer team, vil team1 og team2 feks byttes ut med TeamGraut og TeamWacky objekter, ettersom det er forskjell på dem
    public BotBowsTeam team2 = new TeamSauce();
    private final Set<BotBowsPlayer> players = new HashSet<>(); // liste med alle players som er i gamet
    private int maxHP = 3; // hvor mye hp man har hvis custom hp er disabla
    // win condition
    private boolean dynamicScoring = true; // If true, når alle på et lag dauer så gis et poeng for hvert liv som er igjen + totalt liv som er tatt ut
    private int winScoreThreshold = 5; // hvor mange poeng man skal spille til. Hvis den er 0, så fortsetter det for alltid til man tar /stopgame (/botbows stop)
    private int roundDuration = 0;
    // hazards
    public StormHazard stormHazard = new StormHazard(this); // holder styr på innstillinger og utførelse av storm logikk
    public EarthquakeHazard earthquakeHazard = new EarthquakeHazard(this); // holder styr på innstillinger og utførelse av storm logikk
    public GhostHazard ghostHazard = new GhostHazard(this);
    // abilities
    private int maxAbilities = 2;
    private float abilityCooldownMultiplier = 1.0f;
    private final Map<AbilityType, Boolean> abilityStates = new HashMap<>();
    // menus
    public MapMenu mapMenu;
    public HealthMenu healthMenu;
    public TeamsMenu teamsMenu;
    public WinConditionMenu winConditionMenu;
    public HazardMenu hazardMenu;
    public AbilityMenu abilityMenu;

    private BotBowsPlayer modPlayer;

    public Settings(Lobby lobby) {
        this.lobby = lobby;
        team1.setOppositeTeam(team2); // sånn at hvert team holder styr på hvilket team som er motstanderteamet
        team2.setOppositeTeam(team1);
    }

    public void initMenus() {
        mapMenu = new MapMenu(this);

        healthMenu = new HealthMenu(this);
        healthMenu.disableCustomHP();
        setMaxHP(3);

        teamsMenu = new TeamsMenu(this);
        teamsMenu.registerTeams();

        winConditionMenu = new WinConditionMenu(this);
        winConditionMenu.enableDynamicPoints();
        winConditionMenu.updateWinScoreThreshold();
        winConditionMenu.updateRoundDuration();

        hazardMenu = new HazardMenu(this);
        hazardMenu.initMenu();

        abilityMenu = new AbilityMenu(this);
        abilityMenu.disableAbilities();
    }

    public void setMap(BotBowsMap map) {
        if (map == currentMap) return;
        currentMap = map;
        switch (map) {
            case CLASSIC_ARENA -> setNewTeams(new TeamBlaud(team1), new TeamSauce(team2));
            case ICY_RAVINE -> setNewTeams(new TeamGraut(team1), new TeamWacky(team2));
        }
        team1.postTeamSwap();
        team2.postTeamSwap();
        teamsMenu.registerTeams();
        teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        healthMenu.updateMenu(); // update so the name colors match the new team color
        String mapName = map.name().charAt(0) + map.name().substring(1).toLowerCase().replace('_', ' ');
        lobby.messagePlayers(Component.text("Map set to ").append(Component.text(mapName, NamedTextColor.GREEN)));
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
        team1.setOppositeTeam(team2);
        team2.setOppositeTeam(team1);
    }

    public void joinGame(Player p) {
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        if (bp == null) {
            bp = new BotBowsPlayer(p, this);
            lobby.registerBotBowsPlayer(bp);
        } else if (players.contains(bp)) {
            mapMenu.open(p);
            return;
        }
        players.add(bp);
        if (team1.size() <= team2.size()) { // players fordeles jevnt i lagene
            team1.join(bp);
        } else {
            team2.join(bp);
        }
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();
        abilityMenu.addPlayer(bp);

        if (getPlayers().size() == 1) {
            modPlayer = bp;
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(p.getName() + " has joined BotBows Lobby #" + lobby.ID + " (" + players.size() + ")" +
                    " and will be the settings moderator"));
            mapMenu.open(p);
        } else {
            Bukkit.getOnlinePlayers().forEach(q -> q.sendMessage(p.getName() + " has joined BotBows Lobby #" + lobby.ID + " (" + players.size() + ")"));
        }
    }

    public void leaveGame(BotBowsPlayer p) {
        if (!players.contains(p)) {
            p.player.sendMessage(Component.text("You can't leave when you're not in a game", NamedTextColor.RED));
            return;
        }
        p.leaveGame();
        players.remove(p);
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();
        abilityMenu.removePlayer(p);
        if (playerIsMod(p) && !players.isEmpty()) {
            setModPlayer(players.iterator().next());
        }

        p.player.setGameMode(GameMode.SPECTATOR);
        p.player.sendMessage(Component.text("You left BotBows Lobby #" + lobby.ID, NamedTextColor.YELLOW));
        lobby.messagePlayers(Component.text(p.player.getName() + " has left the lobby (" + players.size() + ")", NamedTextColor.YELLOW));
    }

    public Set<BotBowsPlayer> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isPlayerJoined(Player p) {
        return Optional.ofNullable(lobby.getBotBowsPlayer(p))
                .map(players::contains)
                .orElse(false);
    }

    public void setModPlayer(BotBowsPlayer p) {
        String first = modPlayer == null ? "" : "new ";
        modPlayer = p;
        lobby.messagePlayers(p.player.name().color(NamedTextColor.GREEN).append(Component.text(" is the + " + first + "game mod", NamedTextColor.WHITE)));
    }

    public boolean playerIsMod(BotBowsPlayer p) {
        boolean isPlayerMod = p == modPlayer;
        if (!isPlayerMod) p.player.sendMessage(Component.text("Only mods can do this action", NamedTextColor.RED));
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
        if (this.maxAbilities == 0) {
            this.maxAbilities = maxAbilities;
            abilityMenu.enableAbilities();
        }
        this.maxAbilities = maxAbilities;
        players.forEach(p -> p.setMaxAbilities(maxAbilities));
        abilityMenu.updateMaxAbilities();
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public void setAbilityCooldownMultiplier(float cooldownMultiplier) {
        abilityCooldownMultiplier = cooldownMultiplier;
        players.forEach(p -> p.setAbilityCooldownMultiplier(cooldownMultiplier));
        abilityMenu.updateCooldownMultiplier();
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }

    public void allowAbility(AbilityType type) {
        abilityStates.put(type, true);
        abilityMenu.updateAbilityStatus(type);
    }

    public void disableAbility(AbilityType type) {
        abilityStates.put(type, false);
        abilityMenu.updateAbilityStatus(type);
        players.forEach(p -> p.unequipAbility(type));
    }

    public void toggleAbility(AbilityType type) {
        if (abilityAllowed(type)) {
            disableAbility(type);
        } else {
            allowAbility(type);
        }
    }

    public boolean abilityAllowed(AbilityType type) {
        return abilityStates.getOrDefault(type, true); // Default to enabled
    }
}
