package gruvexp.bbminigames.sumo;

import org.bukkit.entity.Player;

public class PlayerData {

    public final Player PLAYER;
    private int wins = 0;
    private int losses = 0;
    private int score;

    public PlayerData(Player player) {
        this.PLAYER = player;
    }
    public int getWins() {
        return wins;
    }
    public int getLosses() {
        return losses;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

    public void saveResult(boolean win) { // win is a bool telling if you won or lost
        if (win) {
            wins ++;
        } else {
            losses ++;
        }
    }
}
