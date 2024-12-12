package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.menu.menus.*;
import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Settings {
    public BotBowsMap currentMap = BotBowsMap.BLAUD_VS_SAUCE; // default map
    // hazards
    public StormHazard stormHazard = new StormHazard(this); // holder styr på innstillinger og utførelse av storm logikk
    public EarthquakeHazard earthquakeHazard = new EarthquakeHazard(this); // holder styr på innstillinger og utførelse av storm logikk
    public GhostHazard ghostHazard = new GhostHazard(this);

    public BotBowsTeam team1 = new TeamBlaud(); // dersom man endrer team, vil team1 og team2 feks byttes ut med TeamGraut og TeamWacky objekter, ettersom det er forskjell på dem
    public BotBowsTeam team2 = new TeamSauce();
    private final Set<BotBowsPlayer> players = new HashSet<>(); // liste med alle players som er i gamet
    private int maxHP = 3; // hvor mye hp man har hvis custom hp er disabla
    private boolean dynamicScoring = true; // If true, når alle på et lag dauer så gis et poeng for hvert liv som er igjen + totalt liv som er tatt ut
    private int winThreshold = 5; // hvor mange poeng man skal spille til. Hvis den er satt til -1, så fortsetter det for alltid til man tar /stopgame (/botbows stop)
    // abilities
    private int maxAbilities = 2;
    private float abilityCooldownMultiplier = 1.0f;
    // menus
    public MapMenu mapMenu;
    public HealthMenu healthMenu;
    public TeamsMenu teamsMenu;
    public WinThresholdMenu winThresholdMenu;
    public HazardMenu hazardMenu;
    public AbilityMenu abilityMenu;

    public Settings() {
        team1.setOppositeTeam(team2); // sånn at hvert team holder styr på hvilket team som er motstanderteamet
        team2.setOppositeTeam(team1);
    }

    public void initMenus() {
        mapMenu = new MapMenu();

        healthMenu = new HealthMenu();
        healthMenu.disableCustomHP();
        healthMenu.enableDynamicPoints();
        setMaxHP(3);

        teamsMenu = new TeamsMenu();

        winThresholdMenu = new WinThresholdMenu();

        hazardMenu = new HazardMenu();

        abilityMenu = new AbilityMenu();
        abilityMenu.disableAbilities();
    }

    public void setMap(BotBowsMap map) {
        if (map == currentMap) return;
        currentMap = map;
        switch (map) {
            case BLAUD_VS_SAUCE -> setNewTeams(new TeamBlaud(team1), new TeamSauce(team2));
            case GRAUT_VS_WACKY -> setNewTeams(new TeamGraut(team1), new TeamWacky(team2));
        }
        teamsMenu.registerTeams();
        teamsMenu.setColoredGlassPanes(); // update the glass pane items that show the team colors and name
        teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        healthMenu.updateMenu(); // update so the name colors match the new team color
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
    }

    public void joinGame(Player p) {
        BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
        if (bp == null) {
            bp = new BotBowsPlayer(p, this);
            BotBows.registerBotBowsPlayer(bp);
        } else if (players.contains(bp)) {
            p.sendMessage(ChatColor.RED + "You already joined!");
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
        for (Player q : Bukkit.getOnlinePlayers()) {
            q.sendMessage(p.getPlayerListName() + " has joined BotBows Classic! (" + players.size() + ")");
        }
    }

    public void leaveGame(BotBowsPlayer p) {
        if (!players.contains(p)) {
            p.PLAYER.sendMessage(ChatColor.RED + "You can't leave when you're not in a game");
            return;
        }
        p.leaveGame();
        players.remove(p);
        teamsMenu.recalculateTeam();
        healthMenu.updateMenu();

        p.PLAYER.setGameMode(GameMode.SPECTATOR);
        BotBows.messagePlayers(Component.text(p.PLAYER.getName() + " has left the game (" + players.size() + ")", NamedTextColor.YELLOW));
    }

    public Set<BotBowsPlayer> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isPlayerJoined(Player p) {
        return Optional.ofNullable(BotBows.getBotBowsPlayer(p))
                .map(players::contains)
                .orElse(false);
    }

    public void setDynamicScoring(boolean dynamicScoring) {
        this.dynamicScoring = dynamicScoring;
    }

    public boolean dynamicScoringEnabled() {
        return dynamicScoring;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        for (BotBowsPlayer p : players) { // oppdaterer livene til alle playersene
            p.setMaxHP(maxHP);
        }
        healthMenu.updateMenu();
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getWinThreshold() {
        return winThreshold;
    }

    public void changeWinThreshold(int ΔthresholdChange) {
        setWinThreshold(winThreshold + ΔthresholdChange);
    }

    public void setWinThreshold(int threshold) {
        winThreshold = Math.max(threshold, 0);
        winThresholdMenu.updateMenu();
    }

    public void setMaxAbilities(int maxAbilities) {
        this.maxAbilities = maxAbilities;
        players.forEach(p -> p.setMaxAbilities(maxAbilities));
        abilityMenu.updateMaxAbilities();
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public void setAbilityCooldownMultiplier(float cooldownMultiplier) {
        abilityCooldownMultiplier = cooldownMultiplier;
        abilityMenu.updateCooldownMultipliers();
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }
}
