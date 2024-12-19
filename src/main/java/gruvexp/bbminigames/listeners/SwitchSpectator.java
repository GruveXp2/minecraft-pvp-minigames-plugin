package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class SwitchSpectator implements Listener {

    private static void spectateNext(Player p, boolean isOwnTeam) {
        BotBowsTeam team = BotBows.getBotBowsPlayer(p).getTeam();
        if (!isOwnTeam) {
            team = team.getOppositeTeam();
        }
        List<Player> alivePlayers = team.getPlayers().stream() // lager liste med alle de levende playersene
                .map(q -> q.PLAYER)
                .filter(q -> q.getGameMode() == GameMode.ADVENTURE)
                .toList();

        if (alivePlayers.isEmpty()) {
            p.sendMessage(Component.text("Cant spectate, " + team.NAME + " has no alive players", NamedTextColor.GRAY));
            return;
        }

        if (p.getSpectatorTarget() == null) {
            p.setSpectatorTarget(alivePlayers.getFirst());
            //p.sendMessage(ChatColor.GRAY + "Currently spectating no players, spectating " + alive_players.get(0).getPlayerListName() + "(first player in " + team_str + ")");
            return;
        }
        if (team.hasPlayer(BotBows.getBotBowsPlayer((Player) p.getSpectatorTarget()))) {
            int i = alivePlayers.indexOf((Player) p.getSpectatorTarget());
            if (i == alivePlayers.size() - 1) {
                i = -1;
            }
            p.setSpectatorTarget(alivePlayers.get(i + 1)); // spectater den neste playeren
            //p.sendMessage(ChatColor.GRAY + "Already spectating someone from " + team_str + " (" + alive_players.get(alive_players.indexOf((Player) p.getSpectatorTarget())).getPlayerListName() + "), spectating " + alive_players.get(i + 1).getPlayerListName() + "the next player if possible");
        } else {
            p.setSpectatorTarget(alivePlayers.getFirst()); // spectater den første player i enemy team
            //p.sendMessage(ChatColor.GRAY + "Switching to " + team_str + " and spectating " + alive_players.get(0).getPlayerListName() + " (the first player)");
        }
    }

    @EventHandler()
    public void onMouseClick(PlayerInteractEvent e) {
        if (!BotBows.activeGame) return;
        Player p = e.getPlayer();
        if (!BotBows.settings.isPlayerJoined(p)) return;
        if (p.getGameMode() != GameMode.SPECTATOR) return;
        Action a = e.getAction();

        //p.sendMessage(ChatColor.GRAY + "You clicked a button: ");

        if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
            //p.sendMessage(ChatColor.GRAY + "left clicked air or block");
            spectateNext(p, true);
        } else if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            //p.sendMessage(ChatColor.GRAY + "right clicked air or block");
            spectateNext(p, false);
        }
    }

}
