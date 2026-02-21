package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
    private BotBowsTeam oppositeTeam;
    List<BotBowsPlayer> players = new ArrayList<>(4);
    private int points;

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, Location[] spawnPos, Location tribunePos) {
        this.name = name;
        this.color = color;
        this.dyeColor = dyeColor;
        this.spawnPos = spawnPos;
        this.tribunePos = tribunePos;
    }

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, Location spawnPos, Location tribunePos) {
        this.name = name;
        this.color = color;
        this.dyeColor = dyeColor;
        this.spawnPos = new Location[] {spawnPos, spawnPos, spawnPos, spawnPos, spawnPos};
        this.tribunePos = tribunePos;
    }

    public BotBowsTeam(String name, TextColor color, DyeColor dyeColor, Location spawnPos, Location tribunePos, BotBowsTeam otherTeam) {
        this(name, color, dyeColor, spawnPos, tribunePos);
        this.players = otherTeam.players;
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
            players.get(i).player.teleport(spawnPos[i]);
        }
    }

    public void postTeamSwap() { // when the map is changed and the teams are swapped out
        for (BotBowsPlayer p : players) {
            p.updateTeam(this);
            p.player.teleport(tribunePos);
        }
    }

    public void join(BotBowsPlayer p) {
        players.add(p);
        p.player.teleport(tribunePos);
        p.joinTeam(this);
    }

    public void leave(BotBowsPlayer p) {
        players.remove(p);
        p.leaveTeam();
    }

    public void reset() {
        points = 0;
    }

    public int size() {return players.size();}

    public boolean hasPlayer(BotBowsPlayer p) {
        return players.contains(p);
    }

    public BotBowsPlayer getPlayer(int id) {return players.get(id);}

    public int getPlayerID(BotBowsPlayer p) {return players.indexOf(p);}

    public List<BotBowsPlayer> getPlayers() {return players;}

    public boolean isEmpty() {return players.isEmpty();}

    public Location getSpawnPos(BotBowsPlayer p) {
        return spawnPos[players.indexOf(p)];
    }

    public int getPoints() {return points;}

    public void addPoints(int score) {
        points += score;
    }

    public int getHealthPercentage() {
        int totalHealth = 0;
        int currentHealth = 0;
        for (BotBowsPlayer player : players) {
            totalHealth += player.getMaxHP();
            currentHealth += player.getHP();
        }
        double healthLevel = (float) currentHealth / totalHealth;
        return (int) (healthLevel * 100);
    }

    public boolean isEliminated() {
        for (BotBowsPlayer p : players) {
            if (p.getHP() > 0) {
                return false;
            }
        }
        return true;
    }

    public void glow(int seconds) {
        players.forEach(p -> p.avatar.setGlowing(true));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> players.forEach(p -> p.avatar.setGlowing(false)), 20L * seconds);
    }

    public Material getGlassPane() {
        return Material.getMaterial(dyeColor.name() + "_STAINED_GLASS_PANE");
    }

    public TextComponent toComponent() {
        return Component.text(name, color);
    }
}
