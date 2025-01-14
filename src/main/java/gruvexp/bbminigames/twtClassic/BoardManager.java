package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.Color;

public class BoardManager {

    public final Lobby lobby;
    private Objective objective;
    private Team sbTeam1;
    private Team sbTeam2;
    public ScoreboardManager manager = Bukkit.getScoreboardManager();

    public BoardManager(Lobby lobby) {
        this.lobby = lobby;
    }

    public void createBoard() {
        Scoreboard board = manager.getNewScoreboard();
        Component objectiveTitle = Component.text("BotBows").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("Classic").color(NamedTextColor.AQUA));
        objective = board.registerNewObjective("botbows", Criteria.DUMMY, objectiveTitle);

        BotBowsTeam team1 = lobby.settings.team1;
        BotBowsTeam team2 = lobby.settings.team2;
        // setter inn scores
        setScore(toChatColor((NamedTextColor) darkenColor(team2.color)) + "TEAM " + team2.name.toUpperCase(), lobby.settings.team2.size());

        setScore(toChatColor((NamedTextColor) darkenColor(team1.color)) + "TEAM " + team1.name.toUpperCase(), lobby.getTotalPlayers() + 1);
        setScore(ChatColor.GRAY + "----------", lobby.getTotalPlayers() + 2);
        setScore("", lobby.getTotalPlayers() + 5);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Team stuff!
        sbTeam1 = board.registerNewTeam(team1.name);
        sbTeam2 = board.registerNewTeam(team2.name);

        sbTeam1.color((NamedTextColor) team1.color);
        sbTeam2.color((NamedTextColor) team2.color);

        for (BotBowsPlayer p : team1.getPlayers()) {
            sbTeam1.addEntry(p.player.getName());
        }
        for (BotBowsPlayer p : team2.getPlayers()) {
            sbTeam2.addEntry(p.player.getName());
        }
    }

    public void updatePlayerScore(BotBowsPlayer p) {
        removePlayerScore(p);

        int hp = p.getHP();
        int maxHp = p.getMaxHP();
        int playerLineIndex; // which line of the scoreboard the player stats will be shown
        if (lobby.settings.team1.hasPlayer(p)) { //
            playerLineIndex = lobby.settings.team1.getPlayerID(p) + lobby.settings.team2.size() + 1;
        } else {
            playerLineIndex = lobby.settings.team2.getPlayerID(p);
        }

        String healthBar;
        if (maxHp > 5) {
            healthBar = ChatColor.RED + "▏".repeat(hp) + ChatColor.GRAY + "▏".repeat(maxHp - hp) + toChatColor((NamedTextColor) p.getTeam().color) + " " + p.player.getName();
        } else {
            healthBar = ChatColor.RED + "❤".repeat(hp) + ChatColor.GRAY + "❤".repeat(maxHp - hp) + toChatColor((NamedTextColor) p.getTeam().color) + " " + p.player.getName();
        }

        setScore(healthBar, playerLineIndex);
    }

    public void removePlayerScore(BotBowsPlayer p) {
        Scoreboard sb = objective.getScoreboard();
        for (Objective ignored : sb.getObjectives()) {
            for (String entries : sb.getEntries()) {
                if (entries.contains(p.player.getName())) {
                    sb.resetScores(entries);
                }
            }
        }
    }

    public void updateTeamScores() {
        Scoreboard sb = objective.getScoreboard();
        int winThreshold = lobby.settings.getWinThreshold();

        for (Objective ignored : sb.getObjectives()) {
            for (String entries : sb.getEntries()) {
                if (entries.contains(lobby.settings.team1.name + ": ")) {
                    sb.resetScores(entries);
                }
                if (entries.contains(lobby.settings.team2.name + ": ")) {
                    sb.resetScores(entries);
                }
            }
        }
        BotBowsTeam team1 = lobby.settings.team1;
        BotBowsTeam team2 = lobby.settings.team2;
        int totalPlayers = lobby.getTotalPlayers();
        if (winThreshold == -1) {
            setScore(toChatColor((NamedTextColor) team1.color) + team1.name + ": " + ChatColor.RESET + team1.getPoints(), 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(toChatColor((NamedTextColor) team2.color) + team2.name + ": " + ChatColor.RESET + team2.getPoints(), 3 + totalPlayers);
        } else if (winThreshold >= 35) {
            setScore(toChatColor((NamedTextColor) team1.color) + team1.name + ": " + ChatColor.RESET + team1.getPoints() + " / " + ChatColor.GRAY + winThreshold, 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(toChatColor((NamedTextColor) team2.color) + team2.name + ": " + ChatColor.RESET + team2.getPoints() + " / " + ChatColor.GRAY + winThreshold, 3 + totalPlayers);
        } else { // få plass til mest mulig streker
            String healthSymbol = getHealthSymbol(winThreshold);
            int team1Points = Math.min(lobby.settings.getWinThreshold(), team1.getPoints());
            int team2Points = Math.min(lobby.settings.getWinThreshold(), team2.getPoints());

            setScore(toChatColor((NamedTextColor) team1.color) + team1.name + ": " + ChatColor.GREEN + healthSymbol.repeat(team1Points) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team1Points), 4 + totalPlayers); // legger inn scoren til hvert team
            setScore(toChatColor((NamedTextColor) team2.color) + team2.name + ": " + ChatColor.GREEN + healthSymbol.repeat(team2Points) + ChatColor.GRAY + healthSymbol.repeat(winThreshold - team2Points), 3 + totalPlayers);
        }
    }

    private static String getHealthSymbol(int winThreshold) {
        String c = "";
        if (winThreshold < 8) {
            c = "█";
        } else if (winThreshold < 9) {
            c = "▉";
        } else if (winThreshold < 10) {
            c = "▊";
        } else if (winThreshold < 12) {
            c = "▋";
        } else if (winThreshold < 15) {
            c = "▌";
        } else if (winThreshold < 17) {
            c = "▍";
        } else if (winThreshold < 23) {
            c = "▎";
        } else if (winThreshold < 34) {
            c = "▏";
        }
        return c;
    }

    private void setScore(String text, int score) {
        Score l1 = objective.getScore(text);
        l1.setScore(score); //nederst
    }

    public void resetTeams() {
        sbTeam1.unregister();
        sbTeam2.unregister();
    }

    private static TextColor darkenColor(TextColor color) {
        if (color instanceof NamedTextColor) {
            String colorName = color.toString();
            if (colorName.equals("LIGHT_PURPLE")) {
                return NamedTextColor.DARK_PURPLE;
            } else if (colorName.startsWith("LIGHT_")) {
                return NamedTextColor.NAMES.value(colorName.replace("LIGHT_", "").toLowerCase());
            } else {
                return NamedTextColor.NAMES.value(("DARK_" + colorName).toLowerCase());
            }
        } else {
            // rethrn a textcolor which is the input mutiplyed saturation with 1.5, and value with 2/3
            // Extract RGB values
            int rgb = color.value();
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            // Convert to HSV
            float[] hsv = Color.RGBtoHSB(red, green, blue, null);

            // Adjust saturation and value
            hsv[1] = Math.min(1.0f, hsv[1] * 1.5f); // Saturation
            hsv[2] = hsv[2] * 2.0f / 3.0f;         // Value

            // Convert back to RGB
            int darkenedRgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);

            // Create new TextColor
            return TextColor.color(darkenedRgb);
        }
    }

    public static ChatColor toChatColor(NamedTextColor textColor) {
        if (textColor == NamedTextColor.RED) return ChatColor.RED;
        if (textColor == NamedTextColor.BLUE) return ChatColor.BLUE;
        if (textColor == NamedTextColor.GREEN) return ChatColor.GREEN;
        if (textColor == NamedTextColor.YELLOW) return ChatColor.YELLOW;
        if (textColor == NamedTextColor.WHITE) return ChatColor.WHITE;
        if (textColor == NamedTextColor.BLACK) return ChatColor.BLACK;
        if (textColor == NamedTextColor.GRAY) return ChatColor.GRAY;
        if (textColor == NamedTextColor.DARK_GRAY) return ChatColor.DARK_GRAY;
        if (textColor == NamedTextColor.DARK_RED) return ChatColor.DARK_RED;
        if (textColor == NamedTextColor.DARK_BLUE) return ChatColor.DARK_BLUE;
        if (textColor == NamedTextColor.DARK_GREEN) return ChatColor.DARK_GREEN;
        if (textColor == NamedTextColor.DARK_AQUA) return ChatColor.DARK_AQUA;
        if (textColor == NamedTextColor.DARK_PURPLE) return ChatColor.DARK_PURPLE;
        if (textColor == NamedTextColor.GOLD) return ChatColor.GOLD;
        if (textColor == NamedTextColor.AQUA) return ChatColor.AQUA;
        if (textColor == NamedTextColor.LIGHT_PURPLE) return ChatColor.LIGHT_PURPLE;
        return ChatColor.WHITE;
    }
}
