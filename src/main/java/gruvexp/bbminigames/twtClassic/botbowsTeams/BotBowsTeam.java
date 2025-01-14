package gruvexp.bbminigames.twtClassic.botbowsTeams;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class BotBowsTeam {
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
            p.player.teleport(tribunePos);
        }
    }

    public void join(BotBowsPlayer p) {
        players.add(p);
        p.player.teleport(tribunePos);
        p.joinTeam(this);
    }

    public void moveToTeam(BotBowsPlayer p, BotBowsTeam newTeam) {
        players.remove(p);
        newTeam.join(p);
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

    public Material getGlassPane() {
        return Material.getMaterial(dyeColor.name() + "_STAINED_GLASS_PANE");
    }

    public TextComponent toComponent() {
        return Component.text(name, color);
    }
}
