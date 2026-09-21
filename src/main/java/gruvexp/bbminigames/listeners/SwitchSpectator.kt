package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.model.stat.ResultDisplay.Companion.registerPlayerClick
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class SwitchSpectator : Listener {
    @EventHandler
    fun onMouseClick(e: PlayerInteractEvent) {
        val p = e.getPlayer()
        registerPlayerClick(p)
        val bp = BotBows.getBotBowsPlayer(p) ?: return

        if (!bp.lobby.isGameActive) return
        if (bp.isAlive) return

        when (e.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> spectateNext(p, bp, true)
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> spectateNext(p, bp, false)
            Action.PHYSICAL -> {}
        }
    }

    companion object {
        private fun spectateNext(p: Player, bp: BotBowsPlayer, isOwnTeam: Boolean) {
            var team = bp.team
            if (!isOwnTeam) {
                team = team.oppositeTeam
            }
            val alivePlayers = team.players
                .filter(BotBowsPlayer::isAlive)
                .map { it.avatar.entity }

            if (alivePlayers.isEmpty()) {
                p.sendMessage(Component.text("Cant spectate, ${team.displayName} has no alive players", NamedTextColor.GRAY))
                return
            }

            if (p.spectatorTarget == null) {
                p.spectatorTarget = alivePlayers.first()
                //p.sendMessage(ChatColor.GRAY + "Currently spectating no players, spectating " + alive_players.get(0).getPlayerListName() + "(first player in " + team_str + ")");
                return
            }
            val target = p.spectatorTarget!!
            val targetBp = BotBows.getBotBowsPlayer(target.uniqueId)
            if (targetBp != null && team.hasPlayer(targetBp)) {
                var i = alivePlayers.indexOf(p.spectatorTarget as LivingEntity)
                if (i == alivePlayers.size - 1) {
                    i = -1
                }
                p.spectatorTarget = alivePlayers[i + 1] // spectater den neste playeren
                //p.sendMessage(ChatColor.GRAY + "Already spectating someone from " + team_str + " (" + alive_players.get(alive_players.indexOf((Player) p.getSpectatorTarget())).getPlayerListName() + "), spectating " + alive_players.get(i + 1).getPlayerListName() + "the next player if possible");
            } else {
                p.spectatorTarget = alivePlayers.first() // spectater den første player i enemy team
                //p.sendMessage(ChatColor.GRAY + "Switching to " + team_str + " and spectating " + alive_players.get(0).getPlayerListName() + " (the first player)");
            }
        }
    }
}
