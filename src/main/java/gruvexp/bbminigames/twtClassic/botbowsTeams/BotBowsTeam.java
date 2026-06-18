package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BotBowsTeam {
    public final String name;
    public final TextColor color;
    public final DyeColor dyeColor;
    public final Location[] spawnPos;
    public final Location tribunePos;

    private final TeamSide teamSide;
    private BotBowsTeam oppositeTeam;
    List<BotBowsPlayer> players = new ArrayList<>(4);
    private int points;

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, TeamSide teamSide, Location[] spawnPos, Location tribunePos) {
        this.name = name;
        this.color = color;
        this.dyeColor = dyeColor;
        this.teamSide = teamSide;
        this.spawnPos = spawnPos;
        this.tribunePos = tribunePos;
    }

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, TeamSide teamSide, Location spawnPos, Location tribunePos) {
        this.name = name;
        this.color = color;
        this.dyeColor = dyeColor;
        this.teamSide = teamSide;
        this.spawnPos = new Location[] {spawnPos, spawnPos, spawnPos, spawnPos, spawnPos};
        this.tribunePos = tribunePos;
    }

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, TeamSide teamSide, Location spawnPos, Location tribunePos, BotBowsTeam otherTeam) {
        this(name, color, dyeColor, teamSide, spawnPos, tribunePos);
        this.players = otherTeam.players;
    }

    public TeamSide getTeamSide() {
        return teamSide;
    }

    public BotBowsTeam getOppositeTeam() {return oppositeTeam;}

    public void setOppositeTeam(BotBowsTeam oppositeTeam) {
        if (this.oppositeTeam != null) {
            throw new IllegalStateException("This team already has an assigned opposite team");
        }
        this.oppositeTeam = oppositeTeam;
    }

    public void tpPlayersToSpawn() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).teleport(spawnPos[Math.min(i, 4)]);
        }
    }

    public void postTeamSwap() { // when the map is changed and the teams are swapped out
        for (BotBowsPlayer bp : players) {
            bp.updateTeam(this);
            bp.teleport(tribunePos);
        }
    }

    public void join(BotBowsPlayer bp) {
        players.add(bp);
        bp.teleport(tribunePos);
        bp.onTeamJoin(this);
    }

    public void leave(BotBowsPlayer bp) {
        players.remove(bp);
        bp.onTeamLeave();
    }

    public void reset() {
        points = 0;
    }

    public int size() {return players.size();}

    public boolean hasPlayer(BotBowsPlayer bp) {
        return players.contains(bp);
    }

    public BotBowsPlayer getPlayer(int id) {return players.get(id);}

    public int getPlayerID(BotBowsPlayer bp) {return players.indexOf(bp);}

    public List<BotBowsPlayer> getPlayers() {return players;}

    public boolean isEmpty() {return players.isEmpty();}

    public Location getSpawnPos(BotBowsPlayer bp) {
        return spawnPos[players.indexOf(bp)];
    }

    public int getPoints() {return points;}

    public void addPoints(int score) {
        points += score;
    }

    public int getHealthPercentage() {
        int totalHealth = 0;
        int currentHealth = 0;
        for (BotBowsPlayer bp : players) {
            totalHealth += bp.settings.getMaxHealth();
            currentHealth += bp.getHP();
        }
        double healthLevel = (float) currentHealth / totalHealth;
        return (int) (healthLevel * 100);
    }

    public boolean isEliminated() {
        for (BotBowsPlayer bp : players) {
            if (bp.getHP() > 0) {
                return false;
            }
        }
        return true;
    }

    public void glow(int seconds) {
        players.forEach(bp -> bp.avatar.setGlowing(true));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> players.forEach(bp -> bp.avatar.setGlowing(false)), 20L * seconds);
    }

    public void setGlowColor(NamedTextColor color, int ticks) {
        players.forEach(bp -> bp.avatar.setColor(color));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> players.forEach(bp ->  bp.avatar.setColor((NamedTextColor) this.color)), ticks);
    }

    public Material getGlassPane() {
        return Material.getMaterial(dyeColor.name() + "_STAINED_GLASS_PANE");
    }

    public TextComponent toComponent() {
        return Component.text(name, color);
    }
}
