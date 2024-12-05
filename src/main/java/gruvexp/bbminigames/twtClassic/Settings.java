package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.twtClassic.botbowsTeams.*;
import gruvexp.bbminigames.twtClassic.hazard.hazards.EarthquakeHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.GhostHazard;
import gruvexp.bbminigames.twtClassic.hazard.hazards.StormHazard;
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

    public Settings() {
        team1.setOppositeTeam(team2); // sånn at hvert team holder styr på hvilket team som er motstanderteamet
        team2.setOppositeTeam(team1);
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
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMap(BotBowsMap map) {
        if (map == currentMap) return;
        currentMap = map;
        switch (map) {
            case BLAUD_VS_SAUCE -> setNewTeams(new TeamBlaud(team1), new TeamSauce(team2));
            case GRAUT_VS_WACKY -> setNewTeams(new TeamGraut(team1), new TeamWacky(team2));
        }
    }

    private void setNewTeams(BotBowsTeam newTeam1, BotBowsTeam newTeam2) {
        team1 = newTeam1;
        team2 = newTeam2;
        BotBows.teamsMenu.setColoredGlassPanes(); // update the glass pane items that show the team colors and name
        BotBows.teamsMenu.recalculateTeam(); // update the player heads so they have the correct color
        BotBows.healthMenu.updateMenu(); // update so the name colors match the new team color
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
        BotBows.teamsMenu.recalculateTeam();
        BotBows.healthMenu.updateMenu();
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
        BotBows.teamsMenu.recalculateTeam();
        BotBows.healthMenu.updateMenu();

        p.PLAYER.setGameMode(GameMode.SPECTATOR);
        BotBows.messagePlayers(ChatColor.YELLOW + p.PLAYER.getPlayerListName() + " has left the game (" + players.size() + ")");
    }

    public Set<BotBowsPlayer> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public boolean isPlayerJoined(Player p) {
        return Optional.ofNullable(BotBows.getBotBowsPlayer(p))
                .map(players::contains)
                .orElse(false);
    }

    public int getWinThreshold() {
        return winThreshold;
    }

    public void changeWinThreshold(int ΔthresholdChange) {
        setWinThreshold(winThreshold + ΔthresholdChange);
    }

    public void setWinThreshold(int threshold) {
        winThreshold = Math.max(threshold, -1);
        BotBows.winThresholdMenu.updateMenu();
    }
}
