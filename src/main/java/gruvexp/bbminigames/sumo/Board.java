package gruvexp.bbminigames.sumo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;

public class Board {
    //2do: lage hashmap der man storer personal scoreboard med stats
    private static Objective objective;

    public static void createTourneyBoard(ArrayList<Player> players) { //   ▍▍▍▍ PlayerName       3
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null; //assert er bare no test greier for å lage error message når en bug skjer.
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("tourney_board", "dummy",
                ChatColor.translateAlternateColorCodes('&', "&l&6Sumo &r&bTournament"));

        Score l1 = obj.getScore("");
        l1.setScore(players.size() + 1); //øverst
        objective = obj;
        for (Player p : players) { //repeater "|" for hver player. scoren(layer) er hvor mye score(points) de har
            p.sendMessage("There is "+players.size()+" players. These players are:");
            for (Player q : players) {
                p.sendMessage(q.getPlayerListName()+", ");
            }
            updateScore(p);
        }
        for (Player p:Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective = obj;
    }
    static void updateScore(Player p) {

        Objective obj = objective;
        Scoreboard scoreboard = obj.getScoreboard();

        //fjerner scoren hvis den allerede er der, siden man skal bytte den ut med den oppdaterte versjonen
        for (Objective ignored : scoreboard.getObjectives()) {
            for (String entries : scoreboard.getEntries()) {
                if (entries.contains(p.getPlayerListName())) {
                    scoreboard.resetScores(entries);
                }
            }
        }

        String score = getScoreString(p);
        Score entry = obj.getScore(score);
        entry.setScore(SumoData.playerScores.get(p)); //de som har fler points kommer lenger opp

    } //updates the score of the player by looking at p_points data.

    static String getScoreString(Player p) { // Lager string som er på "formen ▍▍▍ ▍▍ Player.name" med farger etter wins & losses & battels left

        int currentRound = SumoData.getRound();
        int tourney = SumoData.getTourney();
        StringBuilder output = new StringBuilder();

        for (int r = 0; r < currentRound + 1; r++) { // r = round. Går gjennom alle rundene og adder scorsene
            PlayerData pd;
            try {
                pd = SumoData.playerPoints.get(r).get(SumoData.playerIDs.get(p));
            } catch (NullPointerException e) {
                for (Player q : Bukkit.getOnlinePlayers()) {
                    q.sendMessage(ChatColor.RED + "ERROR! " + p.getPlayerListName() + " is not registered in the match but is in the system?! bugzzzz tell gruvexp to fix");
                }
                continue;
            }


            int wins;
            int losses;
            if (pd != null) {
                wins = pd.getWins();
                losses = pd.getLosses();
            } else { //hvis det enda ikke har blitt lagra non wins/losses
                wins = 0;
                losses = 0;
            }
            int totalBattels = SumoData.rounds.get(r).get(tourney).size() - 1; // Antall battels i den runda. Vil alltid være players - 1

            output.append(ChatColor.GREEN).append(new String(new char[wins]).replace("\0", "▍"))
                    .append(ChatColor.RED).append(new String(new char[losses]).replace("\0", "▍"))
                    .append(ChatColor.GRAY).append(new String(new char[totalBattels - losses - wins]).replace("\0", "▍")).append(" ");
        }

        output.append(p.getPlayerListName());
        return output.toString();
    } // Lager string som er på "formen ▍▍▍ ▍▍ Player.name" med farger etter wins & losses & battels left

    public static void saveResult(Player p, boolean win) {

        int round = SumoData.getRound();

        SumoData.playerPoints.get(round).get(SumoData.playerIDs.get(p)).saveResult(win);
        updateScore(p);
    }
    public static void reset() { // fjerner scoreboardet som er der nå
        ScoreboardManager sm = Bukkit.getServer().getScoreboardManager();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(sm.getNewScoreboard());
        }
    }
}
